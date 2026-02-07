package com.roulette.domain.admin

import com.roulette.common.ApiResponse
import com.roulette.domain.admin.dto.*
import com.roulette.domain.order.OrderStatus
import com.roulette.domain.product.dto.ProductDetailResponse
import com.roulette.domain.product.dto.ProductListResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "어드민", description = "관리자 전용 API (대시보드, 예산, 상품, 주문, 룰렛 관리)")
@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val adminService: AdminService
) {

    @Operation(
        summary = "대시보드",
        description = """
            오늘의 룰렛 운영 현황을 조회합니다.

            응답 정보:
            - budgetDate: 예산 날짜 (KST)
            - dailyLimit: 일일 예산 한도
            - remaining: 남은 예산
            - usedAmount: 사용한 예산
            - participantCount: 오늘 참여자 수
        """
    )
    @GetMapping("/dashboard")
    fun getDashboard(): ApiResponse<DashboardResponse> {
        val response = adminService.getDashboard()
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "예산 설정 조회",
        description = "현재 기본 일일 예산 설정을 조회합니다. (환경변수 DAILY_BUDGET_DEFAULT)"
    )
    @GetMapping("/budget")
    fun getBudget(): ApiResponse<BudgetResponse> {
        val response = adminService.getBudget()
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "예산 설정 변경",
        description = """
            기본 일일 예산을 변경합니다.

            적용 시점:
            - 다음 날(KST)부터 적용
            - 당일 예산은 변경되지 않음

            제약:
            - 최소값: 1,000p
            - 최대값: 10,000,000p
        """
    )
    @PutMapping("/budget")
    fun updateBudget(
        @Valid @RequestBody request: UpdateBudgetRequest
    ): ApiResponse<UpdateBudgetResponse> {
        val response = adminService.updateBudget(request)
        return ApiResponse.success(response)
    }

    // ===== 상품 CRUD =====

    @Operation(
        summary = "상품 목록 조회 (전체)",
        description = """
            모든 상품을 조회합니다. (활성/비활성, 재고 무관)

            조회 범위:
            - 활성 상품 (isActive = true)
            - 비활성 상품 (isActive = false)
            - 재고 0인 상품 포함
        """
    )
    @GetMapping("/products")
    fun getProducts(): ApiResponse<ProductListResponse> {
        val response = adminService.getProducts()
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "상품 생성",
        description = """
            새로운 상품을 생성합니다.

            필수 필드:
            - name: 상품명 (1~100자)
            - price: 가격 (1p 이상)
            - stock: 재고 (0 이상)
            - description: 설명 (1~500자)
        """
    )
    @PostMapping("/products")
    fun createProduct(
        @Valid @RequestBody request: CreateProductRequest
    ): ApiResponse<ProductDetailResponse> {
        val product = adminService.createProduct(request)
        return ApiResponse.success(product)
    }

    @Operation(
        summary = "상품 수정",
        description = """
            상품 정보를 수정합니다.

            수정 가능 필드:
            - name: 상품명
            - price: 가격
            - stock: 재고
            - description: 설명
            - isActive: 활성 여부

            에러:
            - PRODUCT_NOT_FOUND: 존재하지 않는 상품
        """
    )
    @PutMapping("/products/{id}")
    fun updateProduct(
        @Parameter(description = "상품 ID") @PathVariable id: Long,
        @Valid @RequestBody request: UpdateProductRequest
    ): ApiResponse<ProductDetailResponse> {
        val product = adminService.updateProduct(id, request)
        return ApiResponse.success(product)
    }

    @Operation(
        summary = "상품 삭제",
        description = """
            상품을 삭제합니다.

            삭제 조건:
            - 주문 내역이 없는 상품만 삭제 가능

            에러:
            - PRODUCT_NOT_FOUND: 존재하지 않는 상품
            - PRODUCT_HAS_ORDERS: 주문 내역이 있는 상품
        """
    )
    @DeleteMapping("/products/{id}")
    fun deleteProduct(
        @Parameter(description = "상품 ID") @PathVariable id: Long
    ): ApiResponse<DeleteProductResponse> {
        val response = adminService.deleteProduct(id)
        return ApiResponse.success(response)
    }

    // ===== 주문 관리 =====

    @Operation(
        summary = "주문 목록 조회",
        description = """
            전체 주문 내역을 조회합니다. (모든 사용자)

            필터:
            - status: 주문 상태 필터 (COMPLETED, CANCELLED)
            - 미지정 시 전체 주문 조회

            정렬:
            - 최신순 (createdAt DESC)
        """
    )
    @GetMapping("/orders")
    fun getOrders(
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int,
        @Parameter(description = "주문 상태 필터 (선택)") @RequestParam(required = false) status: OrderStatus?
    ): ApiResponse<AdminOrderListResponse> {
        val response = adminService.getOrders(page, size, status)
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "주문 취소",
        description = """
            주문을 취소하고 포인트를 환불합니다.

            환불 처리:
            - 주문 상태를 CANCELLED로 변경
            - 사용한 포인트의 잔액(balance)을 복원
            - 상품 재고 복구 (+1)

            포인트 복원 정책:
            - 원래 포인트의 잔액을 복원 (OrderPointUsage 기반)
            - 이미 만료된 포인트는 복원되어도 사용 불가

            에러:
            - ORDER_NOT_FOUND: 존재하지 않는 주문
            - ORDER_ALREADY_CANCELLED: 이미 취소된 주문
        """
    )
    @PostMapping("/orders/{id}/cancel")
    fun cancelOrder(
        @Parameter(description = "주문 ID") @PathVariable id: Long
    ): ApiResponse<CancelOrderResponse> {
        val response = adminService.cancelOrder(id)
        return ApiResponse.success(response)
    }

    // ===== 룰렛 관리 =====

    @Operation(
        summary = "룰렛 내역 조회",
        description = """
            룰렛 참여 내역을 조회합니다.

            필터:
            - date: 특정 날짜 (KST) 필터
            - 미지정 시 전체 내역 조회

            정렬:
            - 최신순 (createdAt DESC)
        """
    )
    @GetMapping("/roulette/history")
    fun getRouletteHistory(
        @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") size: Int,
        @Parameter(description = "날짜 필터 (선택, 형식: YYYY-MM-DD)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): ApiResponse<AdminRouletteHistoryResponse> {
        val response = adminService.getRouletteHistory(page, size, date)
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "룰렛 취소",
        description = """
            룰렛 참여를 취소하고 포인트를 회수합니다.

            회수 처리:
            - 룰렛 상태를 CANCELLED로 변경
            - 남은 포인트(balance)를 0으로 회수
            - 이미 사용된 포인트는 회수하지 않음

            예산 복구 정책:
            - 당일 취소(KST): 일일 예산 복구 (+amount)
            - 다음 날 이후 취소: 예산 복구 안 함

            에러:
            - ROULETTE_NOT_FOUND: 존재하지 않는 룰렛 내역
            - ROULETTE_ALREADY_CANCELLED: 이미 취소된 룰렛
        """
    )
    @PostMapping("/roulette/{id}/cancel")
    fun cancelRoulette(
        @Parameter(description = "룰렛 내역 ID") @PathVariable id: Long
    ): ApiResponse<CancelRouletteResponse> {
        val response = adminService.cancelRoulette(id)
        return ApiResponse.success(response)
    }
}
