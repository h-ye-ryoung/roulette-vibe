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
        val requestPath = request.requestURI
        logger.info("🔐 [SessionFilter] Processing: ${request.method} $requestPath")

        // 이미 인증된 경우 스킵
        if (SecurityContextHolder.getContext().authentication != null) {
            logger.info("✅ [SessionFilter] Already authenticated, skipping...")
            filterChain.doFilter(request, response)
            return
        }

        // 1순위: X-Session-ID 헤더 확인 (WebView용 - DB 토큰)
        val customToken = request.getHeader("X-Session-ID")
        if (customToken != null && customToken.isNotBlank()) {
            logger.info("📱 [SessionFilter] X-Session-ID detected: FULL=$customToken")
            logger.info("📱 [SessionFilter] Token length: ${customToken.length}")
            logger.info("📱 [SessionFilter] Token format check: ${customToken.matches(Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"))}")

            // DB에 저장된 모든 토큰 출력 (디버깅용)
            val allTokens = userSessionRepository.findAll()
            logger.info("🗄️ [SessionFilter] Total tokens in DB: ${allTokens.size}")
            allTokens.forEach { session ->
                logger.info("  - Token: ${session.token}, UserId: ${session.userId}, Expires: ${session.expiresAt}, Valid: ${session.isValid()}")
            }

            // DB에서 토큰 조회 (expiresAt > now)
            logger.info("🔍 [SessionFilter] Searching for token: $customToken")
            val userSession = userSessionRepository.findValidToken(customToken)
            if (userSession != null) {
                logger.info("✅ [SessionFilter] Token found in DB: userId=${userSession.userId}, expires=${userSession.expiresAt}")

                // 유효성 검증
                if (userSession.isValid()) {
                    logger.info("✅ [SessionFilter] Token valid! Authenticating user: ${userSession.nickname}")

                    // SecurityContext에 인증 정보 설정
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
                    logger.info("🎉 [SessionFilter] Authentication successful for: ${userSession.nickname}")

                    filterChain.doFilter(request, response)
                    return
                } else {
                    logger.warn("⏰ [SessionFilter] Token expired: ${customToken.take(10)}...")
                }
            } else {
                logger.warn("❌ [SessionFilter] Token not found in DB!")
                logger.warn("❌ [SessionFilter] Searched token: $customToken")
                logger.warn("❌ [SessionFilter] Check if token exists in DB list above")
            }
        } else {
            logger.info("ℹ️ [SessionFilter] No X-Session-ID header, trying HttpSession...")
        }

        // 2순위: 기존 HttpSession 방식 (웹 브라우저용)
        val session = request.getSession(false)
        logger.info("🔍 [SessionFilter] HttpSession check: session=${session?.id}")
        if (session != null) {
            logger.info("📋 [SessionFilter] HttpSession attributes: ${session.attributeNames.toList()}")
            val sessionUser = session.getAttribute("user") as? SessionUser
            logger.info("👤 [SessionFilter] SessionUser from HttpSession: $sessionUser")
            if (sessionUser != null) {
                logger.info("🌐 [SessionFilter] HttpSession found for user: ${sessionUser.nickname}")
                val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
                val authentication = UsernamePasswordAuthenticationToken(
                    sessionUser,
                    null,
                    authorities
                )
                SecurityContextHolder.getContext().authentication = authentication
                logger.info("✅ [SessionFilter] Authentication successful via HttpSession")
            } else {
                logger.warn("❌ [SessionFilter] No 'user' attribute in HttpSession")
            }
        } else {
            logger.warn("❌ [SessionFilter] No HttpSession found (session is null)")
        }

        filterChain.doFilter(request, response)
    }
}
