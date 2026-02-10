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
        // X-Session-ID 헤더가 있으면 Cookie로 변환 (WebView 지원)
        val customSessionId = request.getHeader("X-Session-ID")
        val wrappedRequest = if (customSessionId != null && customSessionId.isNotBlank()) {
            logger.info("[SessionFilter] X-Session-ID detected: ${customSessionId.take(10)}...")
            object : HttpServletRequestWrapper(request) {
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
        val session = wrappedRequest.getSession(false)
        if (session != null) {
            val sessionUser = session.getAttribute("user") as? SessionUser
            if (sessionUser != null && SecurityContextHolder.getContext().authentication == null) {
                logger.info("[SessionFilter] User authenticated: ${sessionUser.nickname}")
                val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
                val authentication = UsernamePasswordAuthenticationToken(
                    sessionUser,
                    null,
                    authorities
                )
                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        filterChain.doFilter(wrappedRequest, response)
    }
}
