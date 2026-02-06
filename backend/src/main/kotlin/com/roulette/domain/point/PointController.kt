package com.roulette.domain.point

import com.roulette.auth.SessionUser
import com.roulette.common.ApiResponse
import com.roulette.domain.point.dto.BalanceResponse
import com.roulette.domain.point.dto.ExpiringPointsResponse
import com.roulette.domain.point.dto.PointHistoryResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user/points")
class PointController(
    private val pointService: PointService
) {

    /**
     * 포인트 잔액 조회
     *
     * 동작:
     * - 유효 포인트(expires_at > NOW()) 합산
     * - 7일 이내 만료 예정 포인트 목록 반환
     */
    @GetMapping("/balance")
    fun getBalance(
        @AuthenticationPrincipal sessionUser: SessionUser
    ): ApiResponse<BalanceResponse> {
        val response = pointService.getBalance(sessionUser.id)
        return ApiResponse.success(response)
    }

    /**
     * 7일 내 만료 예정 포인트 조회
     *
     * 동작:
     * - 7일 이내 만료 예정 포인트 목록
     * - 만료 예정 포인트 잔액 합계
     */
    @GetMapping("/expiring")
    fun getExpiringPoints(
        @AuthenticationPrincipal sessionUser: SessionUser
    ): ApiResponse<ExpiringPointsResponse> {
        val response = pointService.getExpiringPoints(sessionUser.id)
        return ApiResponse.success(response)
    }

    /**
     * 포인트 내역 조회 (페이지네이션) - SPEC 경로
     *
     * 동작:
     * - 전체 내역 조회 (유효/만료 모두 포함)
     * - 최신순 정렬
     * - expired: 만료 여부
     * - expiringSoon: 7일 내 만료 예정 여부
     */
    @GetMapping
    fun getPoints(
        @AuthenticationPrincipal sessionUser: SessionUser,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<PointHistoryResponse> {
        val response = pointService.getHistory(sessionUser.id, page, size)
        return ApiResponse.success(response)
    }

    /**
     * 포인트 내역 조회 (페이지네이션) - 별칭
     *
     * 동작:
     * - 전체 내역 조회 (유효/만료 모두 포함)
     * - 최신순 정렬
     * - expired: 만료 여부
     * - expiringSoon: 7일 내 만료 예정 여부
     */
    @GetMapping("/history")
    fun getHistory(
        @AuthenticationPrincipal sessionUser: SessionUser,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<PointHistoryResponse> {
        val response = pointService.getHistory(sessionUser.id, page, size)
        return ApiResponse.success(response)
    }
}
