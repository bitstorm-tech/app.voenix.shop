package com.jotoai.voenix.shop.cart.api

import com.jotoai.voenix.shop.cart.api.dto.CartDto
import com.jotoai.voenix.shop.cart.api.dto.CartOrderInfo
import com.jotoai.voenix.shop.cart.api.dto.CartSummaryDto

/**
 * Query service for cart module read operations.
 * This interface defines all read-only operations for cart data.
 * It serves as the primary read API for other modules to access cart information.
 */
interface CartQueryService {
    /**
     * Gets or creates an active cart for the user.
     */
    fun getOrCreateActiveCart(userId: Long): CartDto

    /**
     * Gets a cart summary (item count and total price).
     */
    fun getCartSummary(userId: Long): CartSummaryDto

    /**
     * Gets a cart by its ID.
     * Internal method for other modules to access cart data.
     */
    fun getCartById(id: Long): CartDto

    /**
     * Finds an active cart for the specified user.
     * Internal method used by order service.
     */
    fun findActiveCartByUserId(userId: Long): CartDto?

    /**
     * Checks if an active cart exists for the user.
     */
    fun existsActiveCartForUser(userId: Long): Boolean

    /**
     * Gets active cart internal ID for order creation.
     * Returns cart's internal ID and status for order module integration.
     */
    fun getActiveCartForOrder(userId: Long): CartOrderInfo?

    /**
     * Marks a cart as converted after order creation.
     * Internal method for order module integration.
     */
    fun markCartAsConverted(cartId: Long)
}
