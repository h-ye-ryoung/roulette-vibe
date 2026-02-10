package com.roulette.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.web.http.CookieHttpSessionIdResolver
import org.springframework.session.web.http.HttpSessionIdResolver

/**
 * iOS WebView 쿠키 이슈 대응 - 커스텀 세션 ID 리졸버
 *
 * JavaScript에서 Cookie 헤더를 설정할 수 없는 브라우저 보안 정책으로 인해
 * X-Session-ID 커스텀 헤더를 우선적으로 확인하고, 없으면 기본 Cookie 방식 사용.
 */
@Configuration
class CustomHttpSessionIdResolverConfig {

    @Bean
    fun httpSessionIdResolver(): HttpSessionIdResolver {
        return CustomHttpSessionIdResolver()
    }
}

class CustomHttpSessionIdResolver : HttpSessionIdResolver {
    private val cookieResolver = CookieHttpSessionIdResolver()

    override fun resolveSessionIds(request: HttpServletRequest): List<String> {
        // 1순위: X-Session-ID 커스텀 헤더 확인 (WebView용)
        val customSessionId = request.getHeader("X-Session-ID")
        if (customSessionId != null && customSessionId.isNotBlank()) {
            println("[CustomResolver] Using X-Session-ID: ${customSessionId.take(10)}...")
            return listOf(customSessionId)
        }

        // 2순위: 기본 Cookie 방식 (웹 브라우저용)
        val cookieSessionIds = cookieResolver.resolveSessionIds(request)
        if (cookieSessionIds.isNotEmpty()) {
            println("[CustomResolver] Using Cookie: ${cookieSessionIds.first().take(10)}...")
        } else {
            println("[CustomResolver] No session ID found")
        }
        return cookieSessionIds
    }

    override fun setSessionId(request: HttpServletRequest, response: HttpServletResponse, sessionId: String) {
        // 응답에는 항상 Cookie로 설정 (웹 브라우저 호환성)
        cookieResolver.setSessionId(request, response, sessionId)
    }

    override fun expireSession(request: HttpServletRequest, response: HttpServletResponse) {
        cookieResolver.expireSession(request, response)
    }
}
