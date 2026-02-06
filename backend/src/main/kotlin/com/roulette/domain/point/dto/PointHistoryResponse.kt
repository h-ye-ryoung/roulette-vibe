package com.roulette.domain.point.dto

import com.roulette.domain.point.PointLedger
import java.time.LocalDateTime

/**
 * 포인트 내역 조회 응답 (페이지네이션)
 */
data class PointHistoryResponse(
    val items: List<PointItem>,
    val totalCount: Long,
    val currentPage: Int,
    val totalPages: Int
)

/**
 * 포인트 내역 항목
 */
data class PointItem(
    val id: Long,
    val amount: Int,
    val balance: Int,
    val type: String,
    val issuedAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val expired: Boolean
) {
    companion object {
        fun from(ledger: PointLedger): PointItem {
            val now = LocalDateTime.now()
            return PointItem(
                id = ledger.id!!,
                amount = ledger.amount,
                balance = ledger.balance,
                type = ledger.type.name,
                issuedAt = ledger.issuedAt,
                expiresAt = ledger.expiresAt,
                expired = ledger.expiresAt.isBefore(now)
            )
        }
    }
}
