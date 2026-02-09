package com.roulette.domain.point

import com.roulette.auth.SessionUser
import com.roulette.common.ApiResponse
import com.roulette.domain.point.dto.BalanceResponse
import com.roulette.domain.point.dto.ExpiringPointsResponse
import com.roulette.domain.point.dto.PendingRecoveryResponse
import com.roulette.domain.point.dto.PointHistoryResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "포인트 (사용자)", description = "포인트 잔액 및 내역 조회 API")
@RestController
@RequestMapping("/api/user/points")
class PointController(
    private val pointService: PointService
) {

    @Operation(
        summary = "포인트 잔액 조회",
        description = """
            유효한 포인트 잔액과 7일 이내 만료 예정 포인트를 조회합니다.

            포인트 계산:
            - 유효 포인트만 합산 (expires_at > NOW())
            - 만료된 포인트는 잔액에서 제외

            응답 정보:
            - totalBalance: 총 유효 포인트 잔액
            - expiringPoints: 7일 이내 만료 예정 포인트 목록
        """
    )
    @GetMapping("/balance")
    fun getBalance(
        @AuthenticationPrincipal sessionUser: SessionUser
    ): ApiResponse<BalanceResponse> {
        val response = pointService.getBalance(sessionUser.id)
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "만료 예정 포인트 조회",
        description = """
            7일 이내 만료 예정인 포인트 목록과 합계를 조회합니다.

            조회 기준:
            - 유효 포인트 중 만료일이 7일 이내인 항목만 반환
            - 만료일 기준 오름차순 정렬 (빨리 만료되는 순)

            응답 정보:
            - totalExpiringAmount: 7일 내 만료 예정 포인트 합계
            - points: 만료 예정 포인트 상세 목록
        """
    )
    @GetMapping("/expiring")
    fun getExpiringPoints(
        @AuthenticationPrincipal sessionUser: SessionUser
    ): ApiResponse<ExpiringPointsResponse> {
        val response = pointService.getExpiringPoints(sessionUser.id)
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "포인트 내역 조회",
        description = """
            포인트 획득/사용 내역을 페이지네이션으로 조회합니다.

            조회 범위:
            - 유효/만료 포인트 모두 포함
            - 최신순 정렬 (createdAt DESC)

            응답 필드:
            - expired: 만료 여부 (boolean)
            - expiringSoon: 7일 이내 만료 예정 여부 (boolean)
            - balance: 현재 잔액 (사용 후 남은 포인트)
        """
    )
    @GetMapping
    fun getPoints(
        @AuthenticationPrincipal sessionUser: SessionUser,
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<PointHistoryResponse> {
        val response = pointService.getHistory(sessionUser.id, page, size)
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "포인트 내역 조회 (별칭)",
        description = "GET /api/user/points 와 동일한 기능을 제공하는 별칭 엔드포인트입니다."
    )
    @GetMapping("/history")
    fun getHistory(
        @AuthenticationPrincipal sessionUser: SessionUser,
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<PointHistoryResponse> {
        val response = pointService.getHistory(sessionUser.id, page, size)
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "회수 예정 포인트 조회",
        description = """
            룰렛 취소로 인해 회수하지 못한 포인트 목록을 조회합니다.

            회수 예정 포인트란?
            - 룰렛 취소 시 이미 사용된 포인트가 있어 전액 회수하지 못한 경우
            - 부족분을 "회수 예정 포인트"로 기록하고, 다음 포인트 지급 시 자동 차감

            불변 원칙:
            - 회수 예정 포인트가 있으면 사용 가능 잔액 = 0 (주문 불가)
            - 다음 룰렛 참여 시 자동으로 차감되어 정산됨

            응답 정보:
            - totalAmount: 회수 예정 포인트 총액
            - items: 회수 예정 포인트 상세 목록 (룰렛 ID, 금액, 취소 일시)
        """
    )
    @GetMapping("/pending-recovery")
    fun getPendingRecovery(
        @AuthenticationPrincipal sessionUser: SessionUser
    ): ApiResponse<PendingRecoveryResponse> {
        val response = pointService.getPendingRecovery(sessionUser.id)
        return ApiResponse.success(response)
    }
}
