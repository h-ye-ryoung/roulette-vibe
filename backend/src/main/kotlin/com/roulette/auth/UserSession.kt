package com.roulette.auth

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * iOS WebView 쿠키 이슈 대응 - DB 기반 세션 토큰
 *
 * 서버 재시작/슬립 시에도 세션이 유지되도록 DB에 저장
 */
@Entity
@Table(name = "user_sessions")
data class UserSession(
    @Id
    val token: String,  // UUID 기반 토큰

    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false)
    val nickname: String,

    @Column(nullable = false)
    val expiresAt: LocalDateTime,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun isValid(): Boolean {
        return LocalDateTime.now().isBefore(expiresAt)
    }
}
