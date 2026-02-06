package com.roulette.domain.roulette.dto

data class SpinResponse(
    val historyId: Long,
    val amount: Int,
    val remainingBudget: Int,
    val message: String
)
