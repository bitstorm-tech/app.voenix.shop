package com.jotoai.voenix.shop.domain.cart.dto

data class CartSummaryDto(
    val itemCount: Int,
    val totalPrice: Long, // Total price in cents
    val hasItems: Boolean,
)
