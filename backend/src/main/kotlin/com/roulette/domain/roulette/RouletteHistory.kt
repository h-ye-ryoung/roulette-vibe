package com.roulette.domain.roulette

import com.roulette.common.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "roulette_history",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "spin_date"])]
)
class RouletteHistory(
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "spin_date", nullable = false)
    val spinDate: LocalDate,

    @Column(nullable = false)
    val amount: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: RouletteStatus = RouletteStatus.ACTIVE
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    fun cancel() {
        status = RouletteStatus.CANCELLED
    }

    fun isActive(): Boolean = status == RouletteStatus.ACTIVE
}

enum class RouletteStatus {
    ACTIVE, CANCELLED
}
