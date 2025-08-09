package com.jotoai.voenix.shop.cart.api

import com.jotoai.voenix.shop.cart.api.dto.AddToCartRequest
import com.jotoai.voenix.shop.cart.api.dto.CartDto
import com.jotoai.voenix.shop.cart.api.dto.CartOrderInfo
import com.jotoai.voenix.shop.cart.api.dto.UpdateCartItemRequest

/**
 * Main facade for cart module operations.
 * This interface defines all command operations for managing shopping carts.
 */
interface CartFacade {
    /**
     * Adds an item to the user's cart.
     */
    fun addToCart(
        userId: Long,
        request: AddToCartRequest,
    ): CartDto

    /**
     * Updates a cart item quantity or custom data.
     */
    fun updateCartItem(
        userId: Long,
        itemId: Long,
        request: UpdateCartItemRequest,
    ): CartDto

    /**
     * Removes an item from the cart.
     */
    fun removeFromCart(
        userId: Long,
        itemId: Long,
    ): CartDto

    /**
     * Clears all items from the cart.
     */
    fun clearCart(userId: Long): CartDto

    /**
     * Updates prices in active carts to current prices.
     */
    fun refreshCartPrices(userId: Long): CartDto

    /**
     * Refreshes cart prices for order creation.
     * Returns updated cart information with current prices.
     */
    fun refreshCartPricesForOrder(cartId: Long): CartOrderInfo
}
