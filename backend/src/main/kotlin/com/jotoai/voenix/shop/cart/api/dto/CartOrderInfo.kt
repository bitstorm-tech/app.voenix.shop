package com.jotoai.voenix.shop.cart.api.dto

import com.jotoai.voenix.shop.cart.api.enums.CartStatus

/**
 * Cart information needed for order creation.
 * This DTO provides the minimal cart data required by the order module
 * without exposing internal cart entities.
 */
data class CartOrderInfo(
    val id: Long,
    val userId: Long,
    val status: CartStatus,
    val items: List<CartOrderItemInfo>,
    val totalPrice: Long,
    val isEmpty: Boolean,
)

/**
 * Cart item information needed for order creation.
 */
data class CartOrderItemInfo(
    val id: Long,
    val articleId: Long,
    val variantId: Long?,
    val quantity: Int,
    val priceAtTime: Long,
    val totalPrice: Long,
    val generatedImageId: Long?,
    val promptId: Long?,
    val promptText: String?,
    val customData: Map<String, Any>?,
)
