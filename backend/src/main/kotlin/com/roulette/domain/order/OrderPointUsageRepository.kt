package com.roulette.domain.order

import org.springframework.data.jpa.repository.JpaRepository

interface OrderPointUsageRepository : JpaRepository<OrderPointUsage, Long> {
    fun findAllByOrderId(orderId: Long): List<OrderPointUsage>
}
