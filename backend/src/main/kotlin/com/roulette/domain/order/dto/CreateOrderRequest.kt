package com.roulette.domain.order.dto

import jakarta.validation.constraints.Positive

data class CreateOrderRequest(
    @field:Positive(message = "Product ID must be positive")
    val productId: Long
)
