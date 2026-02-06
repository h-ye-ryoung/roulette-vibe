package com.roulette.domain.point.dto

import java.time.LocalDateTime

/**
 * 포인트 잔액 조회 응답
 */
data class BalanceResponse(
    val totalBalance: Int,
    val expiringPoints: List<ExpiringPoint>
)

/**
 * 만료 임박 포인트 (7일 이내)
 */
data class ExpiringPoint(
    val id: Long,
    val balance: Int,
    val expiresAt: LocalDateTime
)
