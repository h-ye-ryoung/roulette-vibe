package com.roulette.domain.roulette.dto

data class SpinResponse(
    val historyId: Long,
    val amount: Int,
    val actualGrantedAmount: Int,  // 실제 지급된 금액 (회수 차감 후)
    val recoveredAmount: Int,      // 회수 예정에서 차감된 금액
    val remainingBudget: Int,
    val message: String
)
