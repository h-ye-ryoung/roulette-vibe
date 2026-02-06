package com.roulette.domain.order

import com.roulette.auth.SessionUser
import com.roulette.common.ApiResponse
import com.roulette.domain.order.dto.CreateOrderRequest
import com.roulette.domain.order.dto.CreateOrderResponse
import com.roulette.domain.order.dto.OrderListResponse
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user/orders")
class OrderController(
    private val orderService: OrderService
) {

    /**
     * 상품 주문
     *
     * 동작:
     * - Product 재고 원자적 차감
     * - 포인트 FIFO 차감 (expires_at ASC)
     * - Order + OrderPointUsage 생성
     */
    @PostMapping
    fun createOrder(
        @AuthenticationPrincipal sessionUser: SessionUser,
        @Valid @RequestBody request: CreateOrderRequest
    ): ApiResponse<CreateOrderResponse> {
        val response = orderService.createOrder(sessionUser.id, request.productId)
        return ApiResponse.success(response)
    }

    /**
     * 내 주문 내역 조회
     *
     * 동작:
     * - 최신순 정렬 (createdAt DESC)
     * - 페이지네이션 지원
     */
    @GetMapping
    fun getOrders(
        @AuthenticationPrincipal sessionUser: SessionUser,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ApiResponse<OrderListResponse> {
        val response = orderService.getOrders(sessionUser.id, page, size)
        return ApiResponse.success(response)
    }
}
