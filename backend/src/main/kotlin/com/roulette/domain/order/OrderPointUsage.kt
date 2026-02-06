package com.roulette.domain.order

import com.roulette.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "order_point_usage")
class OrderPointUsage(
    @Column(name = "order_id", nullable = false)
    val orderId: Long,

    @Column(name = "point_ledger_id", nullable = false)
    val pointLedgerId: Long,

    @Column(name = "used_amount", nullable = false)
    val usedAmount: Int
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}
