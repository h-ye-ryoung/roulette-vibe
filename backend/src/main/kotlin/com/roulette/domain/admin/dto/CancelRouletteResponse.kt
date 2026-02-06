package com.roulette.domain.admin.dto

data class CancelRouletteResponse(
    val historyId: Long,
    val userId: Long,
    val userName: String,
    val originalAmount: Int,
    val reclaimedAmount: Int,
    val alreadyUsedAmount: Int,
    val budgetRestored: Boolean,
    val message: String
)
