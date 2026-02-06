package com.roulette.domain.admin.dto

import com.roulette.domain.roulette.RouletteHistory
import com.roulette.domain.roulette.RouletteStatus
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 어드민 룰렛 참여 내역 응답
 */
data class AdminRouletteHistoryResponse(
    val histories: List<AdminRouletteHistoryItem>,
    val page: Int,
    val size: Int,
    val totalElements: Long
)

/**
 * 어드민 룰렛 참여 항목
 */
data class AdminRouletteHistoryItem(
    val id: Long,
    val userId: Long,
    val userName: String,
    val amount: Int,
    val spinDate: LocalDate,
    val status: RouletteStatus,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(history: RouletteHistory, userName: String): AdminRouletteHistoryItem {
            return AdminRouletteHistoryItem(
                id = history.id,
                userId = history.userId,
                userName = userName,
                amount = history.amount,
                spinDate = history.spinDate,
                status = history.status,
                createdAt = history.createdAt
            )
        }
    }
}
