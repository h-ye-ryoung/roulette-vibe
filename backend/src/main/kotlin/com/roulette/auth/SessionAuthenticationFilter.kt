package com.roulette.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 세션에서 사용자 정보를 복원하여 SecurityContext에 설정하는 필터
 */
@Component
class SessionAuthenticationFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val session = request.getSession(false)
        if (session != null) {
            val sessionUser = session.getAttribute("user") as? SessionUser
            if (sessionUser != null && SecurityContextHolder.getContext().authentication == null) {
                val authorities = listOf(SimpleGrantedAuthority("ROLE_${sessionUser.role}"))
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
