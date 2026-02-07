package com.roulette.domain.product

import com.roulette.common.ApiResponse
import com.roulette.domain.product.dto.ProductDetailResponse
import com.roulette.domain.product.dto.ProductListResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "상품 (사용자)", description = "상품 조회 API")
@RestController
@RequestMapping("/api/user/products")
class ProductController(
    private val productService: ProductService
) {

    @Operation(
        summary = "상품 목록 조회",
        description = """
            구매 가능한 상품 목록을 조회합니다.

            조회 조건:
            - 활성화된 상품만 반환 (isActive = true)
            - 재고가 있는 상품만 반환 (stock > 0)

            응답 정보:
            - id: 상품 ID
            - name: 상품명
            - price: 가격 (포인트)
            - stock: 재고 수량
            - description: 상품 설명
        """
    )
    @GetMapping
    fun getProducts(): ApiResponse<ProductListResponse> {
        val response = productService.getProducts()
        return ApiResponse.success(response)
    }

    @Operation(
        summary = "상품 상세 조회",
        description = """
            특정 상품의 상세 정보를 조회합니다.

            에러:
            - PRODUCT_NOT_FOUND: 존재하지 않는 상품 ID
        """
    )
    @GetMapping("/{id}")
    fun getProduct(@Parameter(description = "상품 ID") @PathVariable id: Long): ApiResponse<ProductDetailResponse> {
        val response = productService.getProduct(id)
        return ApiResponse.success(response)
    }
}
