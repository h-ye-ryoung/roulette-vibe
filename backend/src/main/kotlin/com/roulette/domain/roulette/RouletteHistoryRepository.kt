package com.roulette.domain.roulette

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.Optional

interface RouletteHistoryRepository : JpaRepository<RouletteHistory, Long> {
    fun existsByUserIdAndSpinDate(userId: Long, spinDate: LocalDate): Boolean
    fun findByUserIdAndSpinDate(userId: Long, spinDate: LocalDate): Optional<RouletteHistory>
    fun findAllByUserId(userId: Long): List<RouletteHistory>
}
