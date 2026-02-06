package com.roulette.domain.order.dto

data class CreateOrderResponse(
    val orderId: Long,
    val productName: String,
    val totalPrice: Int,
    val remainingBalance: Int
)
