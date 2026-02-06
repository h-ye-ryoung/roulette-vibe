package com.roulette.domain.order

import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long> {
    fun findAllByUserId(userId: Long): List<Order>
    fun findAllByProductId(productId: Long): List<Order>
}
