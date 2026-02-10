package com.roulette.auth

import com.roulette.domain.user.User
import com.roulette.domain.user.UserRepository
import jakarta.servlet.http.HttpSession
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val userSessionRepository: UserSessionRepository
) {

    @Transactional
    fun login(nickname: String, session: HttpSession): LoginResponse {
        val user = userRepository.findByNickname(nickname)
            .orElseGet { createUser(nickname) }

        // 기존 HttpSession 방식 유지 (웹 브라우저용)
        val sessionUser = SessionUser(
            id = user.id,
            nickname = user.nickname
        )
        session.setAttribute("user", sessionUser)

        // DB 기반 토큰 생성 (WebView용)
        val token = java.util.UUID.randomUUID().toString()
        val userSession = UserSession(
            token = token,
            userId = user.id,
            nickname = user.nickname,
            expiresAt = java.time.LocalDateTime.now().plusDays(30)  // 30일 유효
        )
        userSessionRepository.save(userSession)

        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
        val authentication = UsernamePasswordAuthenticationToken(
            sessionUser,
            null,
            authorities
        )
        SecurityContextHolder.getContext().authentication = authentication

        return LoginResponse(
            id = user.id,
            nickname = user.nickname,
            sessionId = token  // DB 토큰 반환
        )
    }

    private fun createUser(nickname: String): User {
        val user = User(nickname = nickname)
        return userRepository.save(user)
    }
}
