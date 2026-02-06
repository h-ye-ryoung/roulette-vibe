package com.roulette.domain.admin.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class CreateProductRequest(
    @field:NotBlank(message = "Product name is required")
    val name: String,

    @field:NotBlank(message = "Description is required")
    val description: String,

    @field:Min(value = 1, message = "Price must be positive")
    val price: Int,

    @field:Min(value = 0, message = "Stock cannot be negative")
    val stock: Int
)
