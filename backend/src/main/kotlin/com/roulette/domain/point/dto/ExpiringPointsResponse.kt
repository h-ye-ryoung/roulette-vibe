package com.roulette.domain.point.dto

/**
 * 만료 예정 포인트 조회 응답
 */
data class ExpiringPointsResponse(
    val expiringPoints: List<ExpiringPoint>,
    val totalExpiringBalance: Int
)
