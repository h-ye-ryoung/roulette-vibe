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
 * iOS WebView 쿠키 이슈 대응: X-Session-ID 헤더를 JSESSIONID 쿠키로 변환
 */
@Component
class SessionAuthenticationFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // X-Session-ID 헤더가 있으면 세션 ID로 사용 (WebView 지원)
        val customSessionId = request.getHeader("X-Session-ID")
        val wrappedRequest = if (customSessionId != null && customSessionId.isNotBlank()) {
            logger.info("[SessionFilter] X-Session-ID detected: ${customSessionId.take(10)}...")
            object : HttpServletRequestWrapper(request) {
                // 핵심: getRequestedSessionId()를 오버라이드해야 getSession()이 올바른 세션을 찾음
                override fun getRequestedSessionId(): String {
                    return customSessionId
                }

                // isRequestedSessionIdFromCookie도 true로 반환
                override fun isRequestedSessionIdFromCookie(): Boolean {
                    return true
                }

                override fun getCookies(): Array<Cookie>? {
                    val existingCookies = super.getCookies() ?: emptyArray()
                    val sessionCookie = Cookie("JSESSIONID", customSessionId).apply {
                        path = "/"
                        isHttpOnly = true
                        secure = true
                    }
                    return existingCookies + sessionCookie
                }
            }
        } else {
            request
        }

        // 세션에서 사용자 정보 복원
        logger.info("[SessionFilter] Requested Session ID: ${wrappedRequest.requestedSessionId}")
        val session = wrappedRequest.getSession(false)
        if (session != null) {
            logger.info("[SessionFilter] Session found: ${session.id.take(10)}...")
            val sessionUser = session.getAttribute("user") as? SessionUser
            if (sessionUser != null) {
                logger.info("[SessionFilter] User authenticated: ${sessionUser.nickname}")
                if (SecurityContextHolder.getContext().authentication == null) {
                    val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
                    val authentication = UsernamePasswordAuthenticationToken(
                        sessionUser,
                        null,
                        authorities
                    )
                    SecurityContextHolder.getContext().authentication = authentication
                }
            } else {
                logger.warn("[SessionFilter] Session exists but no user attribute found")
            }
        } else {
            logger.warn("[SessionFilter] No session found for requested ID: ${wrappedRequest.requestedSessionId}")
        }

        filterChain.doFilter(wrappedRequest, response)
    }
}
