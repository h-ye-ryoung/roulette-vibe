package com.roulette.domain.roulette

import com.roulette.auth.SessionUser
import com.roulette.common.ApiResponse
import com.roulette.domain.roulette.dto.RouletteStatusResponse
import com.roulette.domain.roulette.dto.SpinResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/user/roulette")
class RouletteController(
    private val rouletteService: RouletteService
) {

    /**
     * 룰렛 참여
     *
     * 동시성 처리:
     * - UNIQUE 제약으로 중복 참여 차단
     * - 조건부 UPDATE로 예산 원자적 차감
     */
    @PostMapping("/spin")
    fun spin(
        @AuthenticationPrincipal sessionUser: SessionUser
    ): ApiResponse<SpinResponse> {
        val response = rouletteService.spin(sessionUser.id)
        return ApiResponse.success(response)
    }

    /**
     * 오늘 참여 여부 및 잔여 예산 조회
     */
    @GetMapping("/status")
    fun getStatus(
        @AuthenticationPrincipal sessionUser: SessionUser
    ): ApiResponse<RouletteStatusResponse> {
        val response = rouletteService.getStatus(sessionUser.id)
        return ApiResponse.success(response)
    }
}
