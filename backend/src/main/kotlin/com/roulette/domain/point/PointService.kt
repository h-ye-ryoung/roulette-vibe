package com.roulette.domain.point

import com.roulette.domain.point.dto.BalanceResponse
import com.roulette.domain.point.dto.ExpiringPoint
import com.roulette.domain.point.dto.ExpiringPointsResponse
import com.roulette.domain.point.dto.PointHistoryResponse
import com.roulette.domain.point.dto.PointItem
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class PointService(
    private val pointLedgerRepository: PointLedgerRepository
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
}
