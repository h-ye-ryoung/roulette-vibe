package com.roulette.domain.order

import com.roulette.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "orders")
class Order(
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "product_id", nullable = false)
    val productId: Long,

    @Column(name = "total_price", nullable = false)
    val totalPrice: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: OrderStatus = OrderStatus.COMPLETED
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    fun cancel() {
        if (status == OrderStatus.CANCELLED) {
            throw IllegalStateException("Order is already cancelled")
        }
        status = OrderStatus.CANCELLED
    }

    fun isCancelled(): Boolean = status == OrderStatus.CANCELLED
}

enum class OrderStatus {
    COMPLETED, CANCELLED
}
