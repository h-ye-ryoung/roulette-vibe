package com.roulette.auth

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UserSessionRepository : JpaRepository<UserSession, String> {

    @Query("SELECT s FROM UserSession s WHERE s.token = :token AND s.expiresAt > :now")
    fun findValidToken(token: String, now: LocalDateTime = LocalDateTime.now()): UserSession?

    @Modifying
    @Query("DELETE FROM UserSession s WHERE s.expiresAt < :now")
    fun deleteExpiredSessions(now: LocalDateTime = LocalDateTime.now()): Int

    fun findByUserId(userId: Long): List<UserSession>
}
