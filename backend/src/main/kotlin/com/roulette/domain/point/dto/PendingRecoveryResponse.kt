package com.roulette.domain.point.dto

import java.time.LocalDateTime

/**
 * 회수 예정 포인트 조회 응답
 */
data class PendingRecoveryResponse(
    val totalAmount: Int,
    val items: List<PendingRecoveryItem>
)

/**
 * 회수 예정 포인트 아이템
 */
data class PendingRecoveryItem(
    val id: Long,
    val rouletteHistoryId: Long,
    val amountToRecover: Int,
    val cancelledAt: LocalDateTime
)
