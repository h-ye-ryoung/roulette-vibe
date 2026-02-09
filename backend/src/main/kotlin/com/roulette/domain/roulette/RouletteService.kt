package com.roulette.domain.roulette

import com.roulette.common.AlreadyParticipatedException
import com.roulette.common.BudgetExhaustedException
import com.roulette.config.AppProperties
import com.roulette.domain.budget.DailyBudget
import com.roulette.domain.budget.DailyBudgetRepository
import com.roulette.domain.point.PointLedger
import com.roulette.domain.point.PointLedgerRepository
import com.roulette.domain.point.PointType
import com.roulette.domain.recovery.PendingPointRecoveryRepository
import com.roulette.domain.roulette.dto.RouletteHistoryDto
import com.roulette.domain.roulette.dto.RouletteStatusResponse
import com.roulette.domain.roulette.dto.SpinResponse
import jakarta.persistence.EntityManager
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random

@Service
class RouletteService(
    private val rouletteHistoryRepository: RouletteHistoryRepository,
    private val dailyBudgetRepository: DailyBudgetRepository,
    private val pointLedgerRepository: PointLedgerRepository,
    private val pendingPointRecoveryRepository: PendingPointRecoveryRepository,
    private val appProperties: AppProperties,
    private val entityManager: EntityManager
) {

    /**
     * 룰렛 참여 (1일 1회, 100~1,000p 랜덤)
     *
     * 동시성 처리:
     * 1. UNIQUE(user_id, spin_date) 제약으로 중복 참여 방지
     * 2. 조건부 UPDATE로 예산 원자적 차감
     * 3. 단일 트랜잭션으로 정합성 보장
     */
    @Transactional
    fun spin(userId: Long): SpinResponse {
        val today = LocalDate.now()

        // 1. 중복 참여 체크 (UNIQUE 제약 전에 명시적 검증)
        if (rouletteHistoryRepository.existsByUserIdAndSpinDate(userId, today)) {
            throw AlreadyParticipatedException()
        }

        // 2. 랜덤 금액 결정 (100p 단위: 100, 200, ..., 1000)
        val amount = Random.nextInt(1, 11) * 100  // 1~10 중 랜덤 선택 후 100 곱하기

        // 3. Lazy 예산 리셋: 오늘 날짜로 DailyBudget 조회 또는 생성
        val budget = getOrCreateTodayBudget(today)

        // 4. 예산 원자적 차감 (조건부 UPDATE)
        val updated = dailyBudgetRepository.decrementRemaining(today, amount)

        if (updated == 0) {
            // 예산 부족 (remaining < amount 또는 remaining < 100)
            throw BudgetExhaustedException()
        }

        // 5. RouletteHistory 저장 (UNIQUE 제약으로 중복 방지)
        val history = try {
            val saved = rouletteHistoryRepository.save(
                RouletteHistory(
                    userId = userId,
                    spinDate = today,
                    amount = amount
                )
            )
            entityManager.flush() // 즉시 DB에 반영하여 UNIQUE 제약 위반 확인
            saved
        } catch (e: DataIntegrityViolationException) {
            // UNIQUE(user_id, spin_date) 위반
            throw AlreadyParticipatedException()
        }

        // 6. 회수 예정 포인트 조회 및 차감 (신규)
        val pendingRecoveries = pendingPointRecoveryRepository.findPendingByUserIdForUpdate(userId)

        var totalRecovered = 0
        var remainingAmount = amount

        for (recovery in pendingRecoveries) {
            if (remainingAmount <= 0) break
            val recovered = recovery.deduct(remainingAmount)
            totalRecovered += recovered
            remainingAmount -= recovered
        }

        val actualGrantedAmount = amount - totalRecovered

        // 7. PointLedger 생성 (balance = 실제 지급액)
        val now = LocalDateTime.now()
        val expiresAt = now.plusDays(30)
        pointLedgerRepository.save(
            PointLedger(
                userId = userId,
                rouletteHistoryId = history.id,
                amount = amount,
                balance = actualGrantedAmount,  // 회수 차감 후 금액
                type = PointType.EARN,
                issuedAt = now,
                expiresAt = expiresAt
            )
        )

        // 8. 회수 차감 내역 기록 (신규)
        if (totalRecovered > 0) {
            pointLedgerRepository.save(
                PointLedger(
                    userId = userId,
                    amount = -totalRecovered,
                    balance = 0,
                    type = PointType.RECOVERY_DEDUCTION,
                    issuedAt = now,
                    expiresAt = expiresAt
                )
            )
        }

        // 9. 차감 후 잔여 예산 조회
        val updatedBudget = dailyBudgetRepository.findByBudgetDate(today)
            ?: throw IllegalStateException("Budget not found after decrement")

        return SpinResponse(
            historyId = history.id,
            amount = amount,
            actualGrantedAmount = actualGrantedAmount,  // 신규 필드
            recoveredAmount = totalRecovered,  // 신규 필드
            remainingBudget = updatedBudget.remaining,
            message = if (totalRecovered > 0) {
                "${amount}p 당첨! (회수 예정 ${totalRecovered}p 차감, 실제 지급 ${actualGrantedAmount}p)"
            } else {
                "${amount}p 당첨!"
            }
        )
    }

    /**
     * 오늘 참여 여부 및 잔여 예산 조회
     */
    @Transactional(readOnly = true)
    fun getStatus(userId: Long): RouletteStatusResponse {
        val today = LocalDate.now()

        // 오늘 참여 이력 조회
        val history = rouletteHistoryRepository.findByUserIdAndSpinDate(userId, today)

        // 오늘 예산 조회 (없으면 기본값)
        val budget = dailyBudgetRepository.findByBudgetDate(today)
        val remainingBudget = budget?.remaining ?: appProperties.dailyBudgetDefault

        return RouletteStatusResponse(
            participated = history != null,
            remainingBudget = remainingBudget,
            history = history?.let {
                RouletteHistoryDto(
                    historyId = it.id,
                    amount = it.amount,
                    spinDate = it.spinDate
                )
            }
        )
    }

    /**
     * Lazy 예산 리셋: 오늘 날짜의 DailyBudget 조회 또는 생성
     *
     * PDP-1: 날짜가 다르면 새 행 생성 (스케줄러 불필요)
     */
    private fun getOrCreateTodayBudget(today: LocalDate): DailyBudget {
        return dailyBudgetRepository.findByBudgetDate(today)
            ?: dailyBudgetRepository.save(
                DailyBudget(
                    budgetDate = today,
                    dailyLimit = appProperties.dailyBudgetDefault,
                    remaining = appProperties.dailyBudgetDefault
                )
            )
    }
}
