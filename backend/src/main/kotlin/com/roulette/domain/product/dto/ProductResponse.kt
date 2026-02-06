package com.roulette.domain.product.dto

import com.roulette.domain.product.Product
import java.time.LocalDateTime

/**
 * 상품 목록 응답
 */
data class ProductListResponse(
    val products: List<ProductItem>
)

/**
 * 상품 항목 (목록용)
 */
data class ProductItem(
    val id: Long,
    val name: String,
    val description: String?,
    val price: Int,
    val stock: Int,
    val imageUrl: String?,
    val isActive: Boolean
) {
    companion object {
        fun from(product: Product): ProductItem {
            return ProductItem(
                id = product.id,
                name = product.name,
                description = product.description,
                price = product.price,
                stock = product.stock,
                imageUrl = product.imageUrl,
                isActive = product.isActive
            )
        }
    }
}

/**
 * 상품 상세 응답
 */
data class ProductDetailResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val price: Int,
    val stock: Int,
    val imageUrl: String?,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(product: Product): ProductDetailResponse {
            return ProductDetailResponse(
                id = product.id,
                name = product.name,
                description = product.description,
                price = product.price,
                stock = product.stock,
                imageUrl = product.imageUrl,
                isActive = product.isActive,
                createdAt = product.createdAt,
                updatedAt = product.updatedAt
            )
        }
    }
}
