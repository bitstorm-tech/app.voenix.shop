package com.jotoai.voenix.shop.cart

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class UpdateCartItemRequest(
    @field:NotNull(message = "Quantity is required")
    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int,
    val customData: Map<String, Any>? = null, // Only for crop data and similar non-FK fields
    val generatedImageId: Long? = null,
    val promptId: Long? = null,
)
