package com.roulette.domain.point

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface PointLedgerRepository : JpaRepository<PointLedger, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p FROM PointLedger p
        WHERE p.userId = :userId
        AND p.balance > 0
        AND p.expiresAt > :now
        ORDER BY p.expiresAt ASC
    """)
    fun findAvailableByUserIdForUpdate(
        @Param("userId") userId: Long,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): List<PointLedger>

    @Query("""
        SELECT COALESCE(SUM(p.balance), 0) FROM PointLedger p
        WHERE p.userId = :userId
        AND p.balance > 0
        AND p.expiresAt > :now
    """)
    fun sumAvailableBalance(
        @Param("userId") userId: Long,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): Int

    fun findByRouletteHistoryId(rouletteHistoryId: Long): PointLedger?

    fun findAllByUserId(userId: Long): List<PointLedger>
}
