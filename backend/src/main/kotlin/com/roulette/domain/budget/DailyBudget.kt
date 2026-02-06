package com.roulette.domain.budget

import com.roulette.common.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(
    name = "daily_budget",
    uniqueConstraints = [UniqueConstraint(columnNames = ["budget_date"])]
)
class DailyBudget(
    @Column(name = "budget_date", nullable = false)
    val budgetDate: LocalDate,

    @Column(name = "daily_limit", nullable = false)
    val dailyLimit: Int,

    @Column(nullable = false)
    var remaining: Int
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    fun canDeduct(amount: Int): Boolean = remaining >= amount
}
