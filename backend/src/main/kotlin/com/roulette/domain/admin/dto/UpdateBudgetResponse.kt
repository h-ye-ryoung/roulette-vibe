package com.roulette.domain.admin.dto

import java.time.LocalDate

data class UpdateBudgetResponse(
    val dailyLimit: Int,
    val effectiveFrom: LocalDate
)
