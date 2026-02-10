
package com.roulette.auth

import com.roulette.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpSession
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@Tag(name = "인증", description = "로그인/로그아웃 API")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @Operation(
        summary = "로그인",
        description = """
            닉네임으로 로그인합니다. 사용자가 없으면 자동으로 생성됩니다.
            - 세션 쿠키(JSESSIONID)가 발급됩니다.
        """
    )
    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
        session: HttpSession
    ): ResponseEntity<ApiResponse<LoginResponse>> {
        // AuthService가 이미 DB 기반 토큰을 sessionId에 포함시킴
        val response = authService.login(request.nickname, session)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @Operation(
        summary = "로그아웃",
        description = "현재 세션을 종료하고 로그아웃합니다."
    )
    @PostMapping("/logout")
    fun logout(session: HttpSession): ResponseEntity<ApiResponse<Unit>> {
        session.invalidate()
        SecurityContextHolder.clearContext()
        return ResponseEntity.ok(ApiResponse.success(Unit))
    }

    @Operation(
        summary = "현재 사용자 정보 조회",
        description = "로그인한 사용자의 정보(ID, 닉네임)를 조회합니다."
    )
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
                    sessionId = null  // /me는 기존 세션 정보 조회이므로 새 토큰 불필요
                )
            )
        )
    }
}
