package com.roulette.domain.product

import com.roulette.common.ApiResponse
import com.roulette.domain.product.dto.ProductDetailResponse
import com.roulette.domain.product.dto.ProductListResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user/products")
class ProductController(
    private val productService: ProductService
) {

    /**
     * 상품 목록 조회
     *
     * 동작:
     * - 활성화된 상품만 반환 (isActive = true)
     * - 재고가 있는 상품만 반환 (stock > 0)
     */
    @GetMapping
    fun getProducts(): ApiResponse<ProductListResponse> {
        val response = productService.getProducts()
        return ApiResponse.success(response)
    }

    /**
     * 상품 상세 조회
     *
     * 동작:
     * - 상품 ID로 상세 정보 조회
     * - 존재하지 않으면 PRODUCT_NOT_FOUND
     */
    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: Long): ApiResponse<ProductDetailResponse> {
        val response = productService.getProduct(id)
        return ApiResponse.success(response)
    }
}
