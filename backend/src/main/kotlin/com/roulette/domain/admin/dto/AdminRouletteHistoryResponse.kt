package com.roulette.domain.admin.dto

import com.roulette.domain.roulette.RouletteHistory
import com.roulette.domain.roulette.RouletteStatus
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 어드민 룰렛 참여 내역 응답
 */
data class AdminRouletteHistoryResponse(
    val items: List<AdminRouletteHistoryItem>,
    val pageInfo: PageInfo
)

/**
 * 페이지 정보
 */
data class PageInfo(
    val currentPage: Int,
    val totalPages: Int,
    val pageSize: Int,
    val totalElements: Long,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

/**
 * 어드민 룰렛 참여 항목
 */
data class AdminRouletteHistoryItem(
    val historyId: Long,
    val userId: Long,
    val userName: String,
    val originalAmount: Int,
    val reclaimedAmount: Int,
    val spinDate: LocalDate,
    val status: RouletteStatus,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(history: RouletteHistory, userName: String, reclaimedAmount: Int): AdminRouletteHistoryItem {
            return AdminRouletteHistoryItem(
                historyId = history.id,
                userId = history.userId,
                userName = userName,
                originalAmount = history.amount,
                reclaimedAmount = reclaimedAmount,
                spinDate = history.spinDate,
                status = history.status,
                createdAt = history.createdAt
            )
        }
    }
}
