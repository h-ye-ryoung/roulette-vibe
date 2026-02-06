package com.roulette.domain.budget

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.Optional

interface DailyBudgetRepository : JpaRepository<DailyBudget, Long> {
    fun findByBudgetDate(budgetDate: LocalDate): Optional<DailyBudget>

    @Modifying
    @Query("""
        UPDATE DailyBudget d
        SET d.remaining = d.remaining - :amount
        WHERE d.budgetDate = :date AND d.remaining >= :amount
    """)
    fun decrementRemaining(
        @Param("date") date: LocalDate,
        @Param("amount") amount: Int
    ): Int

    @Modifying
    @Query("""
        UPDATE DailyBudget d
        SET d.remaining = d.remaining + :amount
        WHERE d.budgetDate = :date
    """)
    fun incrementRemaining(
        @Param("date") date: LocalDate,
        @Param("amount") amount: Int
    ): Int
}
