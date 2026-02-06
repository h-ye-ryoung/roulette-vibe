package com.roulette.domain.order.dto

import com.roulette.domain.order.Order
import com.roulette.domain.product.Product
import java.time.LocalDateTime

data class OrderItemResponse(
    val id: Long,
    val productName: String,
    val totalPrice: Int,
    val status: String,
    val createdAt: LocalDateTime,
    val cancelledAt: LocalDateTime?
) {
    companion object {
        fun from(order: Order, product: Product): OrderItemResponse {
            return OrderItemResponse(
                id = order.id,
                productName = product.name,
                totalPrice = order.totalPrice,
                status = order.status.name,
                createdAt = order.createdAt,
                cancelledAt = if (order.isCancelled()) order.updatedAt else null
            )
        }
    }
}
