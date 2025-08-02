package com.jotoai.voenix.shop.domain.cart.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class AddToCartRequest(
    @field:NotNull(message = "Article ID is required")
    val articleId: Long,
    @field:NotNull(message = "Variant ID is required")
    val variantId: Long,
    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int = 1,
    val customData: Map<String, Any> = emptyMap(),
)
