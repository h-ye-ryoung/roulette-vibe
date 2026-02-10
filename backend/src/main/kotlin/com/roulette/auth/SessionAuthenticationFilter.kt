package com.roulette.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 세션에서 사용자 정보를 복원하여 SecurityContext에 설정하는 필터
 * iOS WebView 쿠키 이슈 대응: X-Session-ID 헤더로 DB 기반 토큰 인증
 */
@Component
class SessionAuthenticationFilter(
    private val userSessionRepository: UserSessionRepository
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 이미 인증된 경우 스킵
        if (SecurityContextHolder.getContext().authentication != null) {
            filterChain.doFilter(request, response)
            return
        }

        // 1순위: X-Session-ID 헤더 확인 (WebView용 - DB 토큰)
        val customToken = request.getHeader("X-Session-ID")
        if (customToken != null && customToken.isNotBlank()) {
            logger.info("[SessionFilter] X-Session-ID detected: ${customToken.take(10)}...")
            val userSession = userSessionRepository.findValidToken(customToken)
            if (userSession != null && userSession.isValid()) {
                logger.info("[SessionFilter] Valid token found for user: ${userSession.nickname}")
                val sessionUser = SessionUser(
                    id = userSession.userId,
                    nickname = userSession.nickname
                )
                val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
                val authentication = UsernamePasswordAuthenticationToken(
                    sessionUser,
                    null,
                    authorities
                )
                SecurityContextHolder.getContext().authentication = authentication
                filterChain.doFilter(request, response)
                return
            } else {
                logger.warn("[SessionFilter] Invalid or expired token: ${customToken.take(10)}...")
            }
        }

        // 2순위: 기존 HttpSession 방식 (웹 브라우저용)
        val session = request.getSession(false)
        if (session != null) {
            val sessionUser = session.getAttribute("user") as? SessionUser
            if (sessionUser != null) {
                logger.info("[SessionFilter] HttpSession found for user: ${sessionUser.nickname}")
                val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
                val authentication = UsernamePasswordAuthenticationToken(
                    sessionUser,
                    null,
                    authorities
                )
                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        filterChain.doFilter(request, response)
    }
}
