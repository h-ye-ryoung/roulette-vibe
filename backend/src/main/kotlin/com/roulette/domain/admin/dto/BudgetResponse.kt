package com.roulette.domain.admin.dto

import java.time.LocalDate

data class BudgetResponse(
    val budgetDate: LocalDate,
    val dailyLimit: Int,
    val remaining: Int
)
