package com.roulette.domain.point

import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    /**
     * 7일 이내 만료 예정 포인트 조회 (balance > 0인 것만)
     */
    @Query("""
        SELECT p FROM PointLedger p
        WHERE p.userId = :userId
        AND p.balance > 0
        AND p.expiresAt > :now
        AND p.expiresAt <= :expiringThreshold
        ORDER BY p.expiresAt ASC
    """)
    fun findExpiringPoints(
        @Param("userId") userId: Long,
        @Param("now") now: LocalDateTime = LocalDateTime.now(),
        @Param("expiringThreshold") expiringThreshold: LocalDateTime
    ): List<PointLedger>

    /**
     * 포인트 내역 조회 (페이지네이션) - 최신순
     */
    @Query("""
        SELECT p FROM PointLedger p
        WHERE p.userId = :userId
        ORDER BY p.issuedAt DESC
    """)
    fun findAllByUserIdOrderByIssuedAtDesc(
        @Param("userId") userId: Long,
        pageable: Pageable
    ): Page<PointLedger>

    fun findByRouletteHistoryId(rouletteHistoryId: Long): PointLedger?

    fun findAllByUserId(userId: Long): List<PointLedger>
}
