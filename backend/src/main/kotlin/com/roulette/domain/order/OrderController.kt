package com.roulette.domain.order

import com.roulette.auth.SessionUser
import com.roulette.common.ApiResponse
import com.roulette.domain.order.dto.CreateOrderRequest
import com.roulette.domain.order.dto.CreateOrderResponse
import com.roulette.domain.order.dto.OrderListResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "주문 (사용자)", description = "상품 주문 및 주문 내역 조회 API")
@RestController
@RequestMapping("/api/user/orders")
class OrderController(
    private val orderService: OrderService
) {

    @Operation(
        summary = "상품 주문",
        description = """
            포인트로 상품을 구매합니다.

            주문 프로세스:
            1. 상품 재고 원자적 차감 (동시성 제어)
            2. 포인트 FIFO 차감 (만료 임박 포인트부터 사용)
            3. 주문 및 포인트 사용 내역 생성

            포인트 차감 규칙:
            - 유효한 포인트만 사용 가능 (expires_at > NOW())
            - 만료일 기준 오름차순으로 차감 (expires_at ASC)
            - 여러 포인트를 조합하여 차감 가능

            에러:
            - PRODUCT_NOT_FOUND: 존재하지 않는 상품
            - PRODUCT_OUT_OF_STOCK: 재고 부족
            - INSUFFICIENT_POINTS: 포인트 부족
        """
    )
    @PostMapping
    fun createOrder(
        @AuthenticationPrincipal sessionUser: SessionUser,
        @Valid @RequestBody request: CreateOrderRequest
    ): ApiResponse<CreateOrderResponse> {
        val response = orderService.createOrder(sessionUser.id, request.productId)
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "주문 내역 조회",
        description = """
            내 주문 내역을 페이지네이션으로 조회합니다.

            정렬:
            - 최신순 정렬 (createdAt DESC)

            응답 정보:
            - id: 주문 ID
            - productName: 상품명
            - productPrice: 상품 가격
            - status: 주문 상태 (COMPLETED, CANCELLED)
            - createdAt: 주문 일시
        """
    )
    @GetMapping
    fun getOrders(
        @AuthenticationPrincipal sessionUser: SessionUser,
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<OrderListResponse> {
        val response = orderService.getOrders(sessionUser.id, page, size)
        return ApiResponse.success(response)
    }
}
