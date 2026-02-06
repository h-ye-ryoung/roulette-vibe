package com.roulette.common

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDetail? = null
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(
            success = true,
            data = data
        )

        fun <T> error(code: String, message: String): ApiResponse<T> = ApiResponse(
            success = false,
            error = ErrorDetail(code, message)
        )
    }
}

data class ErrorDetail(
    val code: String,
    val message: String
)
