package com.jotoai.voenix.shop.cart.api

import com.jotoai.voenix.shop.cart.api.dto.CartOrderInfo

/**
 * Query service for cart module read operations.
 * This interface defines operations used by external modules.
 */
interface CartQueryService {
    /**
     * Gets active cart internal ID for order creation.
     * Returns cart's internal ID and status for order module integration.
     */
    fun getActiveCartForOrder(userId: Long): CartOrderInfo?
}
