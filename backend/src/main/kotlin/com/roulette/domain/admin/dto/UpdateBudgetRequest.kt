package com.roulette.domain.admin.dto

import jakarta.validation.constraints.Min

data class UpdateBudgetRequest(
    @field:Min(value = 1000, message = "Daily limit must be at least 1000")
    val dailyLimit: Int
)
