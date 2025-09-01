package com.jotoai.voenix.shop.cart

/**
 * Unified service interface for all cart module operations.
 * This interface defines both user cart operations and inter-module operations.
 */
interface CartService {
    /**
     * Gets active cart information for order creation.
     * Returns minimal cart data needed by the order module.
     */
    fun getActiveCartForOrder(userId: Long): CartOrderInfo?

    /**
     * Refreshes cart prices for order creation.
     * Updates all item prices to current prices and returns cart order info.
     */
    fun refreshCartPricesForOrder(cartId: Long): CartOrderInfo

    /**
     * Marks a cart as converted after order creation.
     * Used by order module integration.
     */
    fun markCartAsConverted(cartId: Long)

    /**
     * Gets or creates an active cart for the user
     */
    fun getOrCreateActiveCart(userId: Long): CartDto

    /**
     * Gets a cart summary (item count and total price)
     */
    fun getCartSummary(userId: Long): CartSummaryDto

    /**
     * Adds an item to the cart
     */
    fun addToCart(
        userId: Long,
        request: AddToCartRequest,
    ): CartDto

    /**
     * Updates a cart item quantity or custom data
     */
    fun updateCartItem(
        userId: Long,
        itemId: Long,
        request: UpdateCartItemRequest,
    ): CartDto

    /**
     * Removes an item from the cart
     */
    fun removeFromCart(
        userId: Long,
        itemId: Long,
    ): CartDto

    /**
     * Clears all items from the cart
     */
    fun clearCart(userId: Long): CartDto

    /**
     * Updates prices in active carts to current prices
     */
    fun refreshCartPrices(userId: Long): CartDto
}
