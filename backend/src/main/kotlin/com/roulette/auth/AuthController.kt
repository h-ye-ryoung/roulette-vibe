
package com.roulette.auth

import com.roulette.common.ApiResponse
import jakarta.servlet.http.HttpSession
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        session: HttpSession
    ): ResponseEntity<ApiResponse<LoginResponse>> {
        val response = authService.login(request.nickname, session)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PostMapping("/logout")
    fun logout(session: HttpSession): ResponseEntity<ApiResponse<Unit>> {
        session.invalidate()
        SecurityContextHolder.clearContext()
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }

    @GetMapping("/me")
    fun me(session: HttpSession): ResponseEntity<ApiResponse<LoginResponse>> {
        val userInfo = session.getAttribute("user") as? SessionUser
            ?: return ResponseEntity.status(401)
                .body(ApiResponse.error("UNAUTHORIZED", "Not logged in"))

        return ResponseEntity.ok(
            ApiResponse.success(
                LoginResponse(
                    id = userInfo.id,
                    nickname = userInfo.nickname,
                    role = userInfo.role
                )
            )
        )
    }
}
