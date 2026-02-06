package com.roulette.domain.order

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long> {
    fun findAllByUserId(userId: Long): List<Order>
    fun findAllByProductId(productId: Long): List<Order>
    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): Page<Order>
}
