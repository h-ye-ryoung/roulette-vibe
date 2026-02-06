package com.roulette.domain.product

import com.roulette.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "products")
class Product(
    @Column(nullable = false, length = 100)
    var name: String,

    @Column(length = 500)
    var description: String? = null,

    @Column(nullable = false)
    var price: Int,

    @Column(nullable = false)
    var stock: Int,

    @Column(name = "image_url", length = 500)
    var imageUrl: String? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    fun hasStock(): Boolean = stock > 0

    fun decrementStock() {
        if (stock <= 0) {
            throw IllegalStateException("Stock is already 0")
        }
        stock--
    }

    fun incrementStock() {
        stock++
    }

    fun deactivate() {
        isActive = false
    }

    fun activate() {
        isActive = true
    }
}
