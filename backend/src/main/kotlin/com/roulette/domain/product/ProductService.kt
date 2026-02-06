package com.roulette.domain.product

import com.roulette.common.ProductNotFoundException
import com.roulette.domain.product.dto.ProductDetailResponse
import com.roulette.domain.product.dto.ProductItem
import com.roulette.domain.product.dto.ProductListResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ProductService(
    private val productRepository: ProductRepository
) {

    /**
     * 상품 목록 조회
     * - 활성화된 상품만 반환 (isActive = true)
     * - 재고가 있는 상품만 반환 (stock > 0)
     */
    fun getProducts(): ProductListResponse {
        val products = productRepository.findAllByIsActiveTrueAndStockGreaterThan(0)
        val productItems = products.map { ProductItem.from(it) }
        return ProductListResponse(productItems)
    }

    /**
     * 상품 상세 조회
     * - 존재하지 않으면 PRODUCT_NOT_FOUND
     */
    fun getProduct(productId: Long): ProductDetailResponse {
        val product = productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException() }

        return ProductDetailResponse.from(product)
    }
}
