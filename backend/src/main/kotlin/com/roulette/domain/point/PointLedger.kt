package com.roulette.domain.point

import com.roulette.common.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

enum class PointType {
    EARN,      // 룰렛으로 획득 (증가, +)
    REFUND,    // 주문 취소로 환불 (증가, +)
    USED,      // 상품 주문으로 사용 (차감, -)
    RECLAIMED  // 룰렛 취소로 회수 (차감, -)
}

@Entity
@Table(name = "point_ledger")
class PointLedger(
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "roulette_history_id")
    val rouletteHistoryId: Long? = null,

    @Column(nullable = false)
    val amount: Int,

    @Column(nullable = false)
    var balance: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: PointType = PointType.EARN,

    @Column(name = "issued_at", nullable = false)
    val issuedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

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
