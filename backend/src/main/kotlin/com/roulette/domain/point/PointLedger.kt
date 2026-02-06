package com.roulette.domain.point

import com.roulette.common.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "point_ledger")
class PointLedger(
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "roulette_history_id", nullable = false)
    val rouletteHistoryId: Long,

    @Column(nullable = false)
    val amount: Int,

    @Column(nullable = false)
    var balance: Int,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)

    fun isAvailable(): Boolean = !isExpired() && balance > 0

    fun deduct(deductAmount: Int): Int {
        val actualDeduct = minOf(deductAmount, balance)
        balance -= actualDeduct
        return actualDeduct
    }

    fun restore(restoreAmount: Int) {
        balance = minOf(balance + restoreAmount, amount)
    }
}
