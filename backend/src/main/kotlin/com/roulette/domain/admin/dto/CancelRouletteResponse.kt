package com.roulette.domain.admin.dto

data class CancelRouletteResponse(
    val historyId: Long,
    val userId: Long,
    val userName: String,
    val originalAmount: Int,
    val reclaimedAmount: Int,
    val alreadyUsedAmount: Int,
    val budgetRestored: Boolean,
    val pendingRecoveryAmount: Int,  // 회수 예정 포인트 금액
    val pendingRecoveryId: Long?,    // 회수 예정 레코드 ID (없으면 null)
    val message: String
)
