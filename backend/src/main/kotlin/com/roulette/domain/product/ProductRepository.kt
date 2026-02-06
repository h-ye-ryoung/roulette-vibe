package com.roulette.domain.product

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProductRepository : JpaRepository<Product, Long> {

    fun findAllByIsActiveTrue(): List<Product>

    fun findAllByIsActiveTrueAndStockGreaterThan(stock: Int): List<Product>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Product p
        SET p.stock = p.stock - 1
        WHERE p.id = :productId AND p.stock > 0
    """)
    fun decrementStock(@Param("productId") productId: Long): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Product p
        SET p.stock = p.stock + 1
        WHERE p.id = :productId
    """)
    fun incrementStock(@Param("productId") productId: Long): Int
}
