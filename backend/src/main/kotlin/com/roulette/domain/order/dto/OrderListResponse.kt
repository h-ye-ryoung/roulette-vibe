package com.roulette.domain.order.dto

data class OrderListResponse(
    val orders: List<OrderItemResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long
)
