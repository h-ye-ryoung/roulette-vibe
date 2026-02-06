package com.roulette.domain.admin.dto

import java.time.LocalDate

data class DashboardResponse(
    val budgetDate: LocalDate,
    val dailyLimit: Int,
    val remaining: Int,
    val usedAmount: Int,
    val participantCount: Int
)
