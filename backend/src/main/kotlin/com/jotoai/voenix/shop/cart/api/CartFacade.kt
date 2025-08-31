package com.jotoai.voenix.shop.cart.api

import com.jotoai.voenix.shop.cart.api.dto.CartOrderInfo

/**
 * Public facade for cart module operations.
 * This interface defines operations used by other modules.
 */
interface CartFacade {
    /**
     * Refreshes cart prices for order creation.
     * Returns updated cart information with current prices.
     */
    fun refreshCartPricesForOrder(cartId: Long): CartOrderInfo

    /**
     * Marks a cart as converted after order creation.
     * Used by order module integration.
     */
    fun markCartAsConverted(cartId: Long)
}
