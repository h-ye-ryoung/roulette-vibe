package com.roulette.domain.admin

import com.roulette.common.ApiResponse
import com.roulette.domain.admin.dto.*
import com.roulette.domain.order.OrderStatus
import com.roulette.domain.product.dto.ProductDetailResponse
import com.roulette.domain.product.dto.ProductListResponse
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val adminService: AdminService
) {

    @GetMapping("/dashboard")
    fun getDashboard(): ApiResponse<DashboardResponse> {
        val response = adminService.getDashboard()
        return ApiResponse.success(response)
    }

    @GetMapping("/budget")
    fun getBudget(): ApiResponse<BudgetResponse> {
        val response = adminService.getBudget()
        return ApiResponse.success(response)
    }

    @PutMapping("/budget")
    fun updateBudget(
        @Valid @RequestBody request: UpdateBudgetRequest
    ): ApiResponse<UpdateBudgetResponse> {
        val response = adminService.updateBudget(request)
        return ApiResponse.success(response)
    }

    // ===== 상품 CRUD =====

    @GetMapping("/products")
    fun getProducts(): ApiResponse<ProductListResponse> {
        val response = adminService.getProducts()
        return ApiResponse.success(response)
    }

    @PostMapping("/products")
    fun createProduct(
        @Valid @RequestBody request: CreateProductRequest
    ): ApiResponse<ProductDetailResponse> {
        val product = adminService.createProduct(request)
        return ApiResponse.success(product)
    }

    @PutMapping("/products/{id}")
    fun updateProduct(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateProductRequest
    ): ApiResponse<ProductDetailResponse> {
        val product = adminService.updateProduct(id, request)
        return ApiResponse.success(product)
    }

    @DeleteMapping("/products/{id}")
    fun deleteProduct(
        @PathVariable id: Long
    ): ApiResponse<DeleteProductResponse> {
        val response = adminService.deleteProduct(id)
        return ApiResponse.success(response)
    }

    // ===== 주문 관리 =====

    @GetMapping("/orders")
    fun getOrders(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) status: OrderStatus?
    ): ApiResponse<AdminOrderListResponse> {
        val response = adminService.getOrders(page, size, status)
        return ApiResponse.success(response)
    }

    @PostMapping("/orders/{id}/cancel")
    fun cancelOrder(
        @PathVariable id: Long
    ): ApiResponse<CancelOrderResponse> {
        val response = adminService.cancelOrder(id)
        return ApiResponse.success(response)
    }

    // ===== 룰렛 관리 =====

    @GetMapping("/roulette/history")
    fun getRouletteHistory(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): ApiResponse<AdminRouletteHistoryResponse> {
        val response = adminService.getRouletteHistory(page, size, date)
        return ApiResponse.success(response)
    }

    @PostMapping("/roulette/{id}/cancel")
    fun cancelRoulette(
        @PathVariable id: Long
    ): ApiResponse<CancelRouletteResponse> {
        val response = adminService.cancelRoulette(id)
        return ApiResponse.success(response)
    }
}
