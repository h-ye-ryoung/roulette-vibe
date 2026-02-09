package com.roulette.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * iOS WebView 쿠키 이슈 대응 필터
 *
 * JavaScript에서 Cookie 헤더를 직접 설정할 수 없는 브라우저 보안 정책으로 인해
 * X-Session-ID 커스텀 헤더로 세션 ID를 전달받아 Cookie로 변환합니다.
 */
@Component
class CustomSessionFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val customSessionId = request.getHeader("X-Session-ID")

        if (customSessionId != null && customSessionId.isNotBlank()) {
            // X-Session-ID 헤더가 있으면 Cookie로 변환
            val wrappedRequest = object : HttpServletRequestWrapper(request) {
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
            filterChain.doFilter(wrappedRequest, response)
        } else {
            filterChain.doFilter(request, response)
        }
    }
}
