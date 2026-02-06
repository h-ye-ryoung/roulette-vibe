package com.roulette.domain.admin.dto

data class CancelOrderResponse(
    val orderId: Long,
    val userId: Long,
    val userName: String,
    val refundedAmount: Int,
    val message: String
)
