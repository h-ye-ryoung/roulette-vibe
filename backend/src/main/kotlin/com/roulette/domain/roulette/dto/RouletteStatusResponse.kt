package com.roulette.domain.roulette.dto

import java.time.LocalDate

data class RouletteStatusResponse(
    val participated: Boolean,
    val remainingBudget: Int,
    val history: RouletteHistoryDto?
)

data class RouletteHistoryDto(
    val historyId: Long,
    val amount: Int,
    val spinDate: LocalDate
)
