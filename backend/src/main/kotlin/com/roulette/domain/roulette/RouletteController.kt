package com.roulette.domain.roulette

import com.roulette.auth.SessionUser
import com.roulette.common.ApiResponse
import com.roulette.domain.roulette.dto.RouletteStatusResponse
import com.roulette.domain.roulette.dto.SpinResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "룰렛 (사용자)", description = "룰렛 참여 및 상태 조회 API")
@RestController
@RequestMapping("/api/user/roulette")
class RouletteController(
    private val rouletteService: RouletteService
) {

    @Operation(
        summary = "룰렛 참여",
        description = """
            100p ~ 1,000p 사이의 랜덤 포인트를 획득합니다.

            제약 조건:
            - 1일 1회 참여 제한 (KST 기준)
            - 일일 예산 소진 시 참여 불가
            - 예산 100p 미만 시 참여 불가

            동시성 처리:
            - UNIQUE 제약으로 중복 참여 차단
            - 조건부 UPDATE로 예산 원자적 차감
        """
    )
    @PostMapping("/spin")
    fun spin(
        @AuthenticationPrincipal sessionUser: SessionUser
    ): ApiResponse<SpinResponse> {
        val response = rouletteService.spin(sessionUser.id)
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "룰렛 참여 상태 조회",
        description = """
            오늘 룰렛 참여 여부와 일일 예산 정보를 조회합니다.

            응답 정보:
            - hasParticipatedToday: 오늘 참여 여부
            - remainingBudget: 오늘 남은 예산
            - canParticipate: 참여 가능 여부 (참여 안 했고 예산 >= 100p)
        """
    )
    @GetMapping("/status")
    fun getStatus(
        @AuthenticationPrincipal sessionUser: SessionUser
    ): ApiResponse<RouletteStatusResponse> {
        val response = rouletteService.getStatus(sessionUser.id)
        return ApiResponse.success(response)
    }
}
