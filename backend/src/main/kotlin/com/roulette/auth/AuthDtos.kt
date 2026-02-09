package com.roulette.auth

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.io.Serializable

data class LoginRequest(
    @field:NotBlank(message = "Nickname is required")
    @field:Size(min = 1, max = 50, message = "Nickname must be between 1 and 50 characters")
    val nickname: String
)

data class LoginResponse(
    val id: Long,
    val nickname: String,
    val sessionId: String? = null
)

data class SessionUser(
    val id: Long,
    val nickname: String
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
