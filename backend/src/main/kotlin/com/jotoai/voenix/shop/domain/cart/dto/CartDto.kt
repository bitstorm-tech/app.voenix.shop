package com.jotoai.voenix.shop.domain.cart.dto

import com.jotoai.voenix.shop.domain.cart.enums.CartStatus
import java.time.OffsetDateTime

data class CartDto(
    val id: Long,
    val userId: Long,
    val status: CartStatus,
    val version: Long,
    val expiresAt: OffsetDateTime?,
    val items: List<CartItemDto>,
    val totalItemCount: Int,
    val totalPrice: Long, // Total price in cents
    val isEmpty: Boolean,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
