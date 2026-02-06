package com.roulette.domain.admin.dto

import com.roulette.domain.order.Order
import com.roulette.domain.order.OrderStatus
import java.time.LocalDateTime

/**
 * 어드민 주문 목록 응답
 */
data class AdminOrderListResponse(
    val orders: List<AdminOrderItem>,
    val page: Int,
    val size: Int,
    val totalElements: Long
)

/**
 * 어드민 주문 항목
 */
data class AdminOrderItem(
    val id: Long,
    val userId: Long,
    val userName: String,
    val productId: Long,
    val productName: String,
    val totalPrice: Int,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
    val cancelledAt: LocalDateTime?
) {
    companion object {
        fun from(order: Order, userName: String, productName: String): AdminOrderItem {
            return AdminOrderItem(
                id = order.id,
                userId = order.userId,
                userName = userName,
                productId = order.productId,
                productName = productName,
                totalPrice = order.totalPrice,
                status = order.status,
                createdAt = order.createdAt,
                cancelledAt = null // Order 엔티티에 cancelledAt이 없으므로 null
            )
        }
    }
}
