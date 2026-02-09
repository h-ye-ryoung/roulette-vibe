package com.roulette.domain.point

import com.roulette.domain.point.dto.BalanceResponse
import com.roulette.domain.point.dto.ExpiringPoint
import com.roulette.domain.point.dto.ExpiringPointsResponse
import com.roulette.domain.point.dto.PendingRecoveryResponse
import com.roulette.domain.point.dto.PendingRecoveryItem
import com.roulette.domain.point.dto.PointHistoryResponse
import com.roulette.domain.point.dto.PointItem
import com.roulette.domain.recovery.PendingPointRecoveryRepository
import com.roulette.domain.recovery.RecoveryStatus
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class PointService(
    private val pointLedgerRepository: PointLedgerRepository,
    private val pendingPointRecoveryRepository: PendingPointRecoveryRepository
) {

    /**
     * 포인트 잔액 조회
     * - 유효 포인트(expires_at > NOW()) 합산
     * - 7일 이내 만료 예정 포인트 목록
     */
    fun getBalance(userId: Long): BalanceResponse {
        val now = LocalDateTime.now()
        val expiringThreshold = now.plusDays(7)

        // 유효 포인트 잔액 합계
        val totalBalance = pointLedgerRepository.sumAvailableBalance(userId, now)

        // 7일 이내 만료 예정 포인트
        val expiringPointsList = pointLedgerRepository.findExpiringPoints(
            userId = userId,
            now = now,
            expiringThreshold = expiringThreshold
        ).map { ledger ->
            ExpiringPoint(
                id = ledger.id!!,
                balance = ledger.balance,
                expiresAt = ledger.expiresAt
            )
        }

        return BalanceResponse(
            totalBalance = totalBalance,
            expiringPoints = expiringPointsList
        )
    }

    /**
     * 7일 내 만료 예정 포인트 조회
     */
    fun getExpiringPoints(userId: Long): ExpiringPointsResponse {
        val now = LocalDateTime.now()
        val expiringThreshold = now.plusDays(7)

        // 7일 이내 만료 예정 포인트
        val expiringPointsList = pointLedgerRepository.findExpiringPoints(
            userId = userId,
            now = now,
            expiringThreshold = expiringThreshold
        ).map { ledger ->
            ExpiringPoint(
                id = ledger.id!!,
                balance = ledger.balance,
                expiresAt = ledger.expiresAt
            )
        }

        val totalExpiringBalance = expiringPointsList.sumOf { it.balance }

        return ExpiringPointsResponse(
            expiringPoints = expiringPointsList,
            totalExpiringBalance = totalExpiringBalance
        )
    }

    /**
     * 포인트 내역 조회 (페이지네이션)
     * - 전체 내역 (유효/만료 모두 포함)
     * - 최신순 정렬
     */
    fun getHistory(userId: Long, page: Int, size: Int): PointHistoryResponse {
        val pageable = PageRequest.of(page, size)
        val pointPage = pointLedgerRepository.findAllByUserIdOrderByIssuedAtDesc(userId, pageable)

        val items = pointPage.content.map { PointItem.from(it) }

        return PointHistoryResponse(
            items = items,
            totalCount = pointPage.totalElements,
            currentPage = pointPage.number,
            totalPages = pointPage.totalPages
        )
    }

    /**
     * 회수 예정 포인트 조회
     *
     * 룰렛 취소 시 회수하지 못한 포인트 목록을 조회합니다.
     * 다음 포인트 지급 시 자동으로 차감됩니다.
     */
    fun getPendingRecovery(userId: Long): PendingRecoveryResponse {
        val pendingList = pendingPointRecoveryRepository.findByUserIdAndStatusOrderByCreatedAtAsc(
            userId, RecoveryStatus.PENDING
        )

        val totalAmount = pendingList.sumOf { it.amountToRecover }

        return PendingRecoveryResponse(
            totalAmount = totalAmount,
            items = pendingList.map { recovery ->
                PendingRecoveryItem(
                    id = recovery.id ?: 0,
                    rouletteHistoryId = recovery.rouletteHistoryId,
                    amountToRecover = recovery.amountToRecover,
                    cancelledAt = recovery.cancelledAt
                )
            }
        )
    }

    /**
     * 유효 포인트 잔액 조회 (회수 예정 고려)
     *
     * 불변 원칙: 회수 예정 포인트가 있으면 사용 가능 잔액 = 0
     * 이는 회수 예정이 있는 유저가 주문을 하지 못하도록 방지합니다.
     */
    fun getEffectiveBalance(userId: Long): Int {
        val now = LocalDateTime.now()
        val availableBalance = pointLedgerRepository.sumAvailableBalance(userId, now)
        val pendingAmount = pendingPointRecoveryRepository.sumPendingAmount(userId)

        // 불변 원칙: 회수 예정이 있으면 사용 가능 잔액 = 0
        return if (pendingAmount > 0) 0 else availableBalance
    }
}
