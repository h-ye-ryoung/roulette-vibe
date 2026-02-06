package com.roulette.auth

import com.roulette.config.AppProperties
import com.roulette.domain.user.Role
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
    private val appProperties: AppProperties
) {

    @Transactional
    fun login(nickname: String, session: HttpSession): LoginResponse {
        val user = userRepository.findByNickname(nickname)
            .orElseGet { createUser(nickname) }

        val sessionUser = SessionUser(
            id = user.id,
            nickname = user.nickname,
            role = user.role.name
        )
        session.setAttribute("user", sessionUser)

        val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
        val authentication = UsernamePasswordAuthenticationToken(
            sessionUser,
            null,
            authorities
        )
        SecurityContextHolder.getContext().authentication = authentication

        return LoginResponse(
            id = user.id,
            nickname = user.nickname,
            role = user.role.name
        )
    }

    private fun createUser(nickname: String): User {
        val role = if (appProperties.isAdmin(nickname)) Role.ADMIN else Role.USER
        val user = User(nickname = nickname, role = role)
        return userRepository.save(user)
    }
}
