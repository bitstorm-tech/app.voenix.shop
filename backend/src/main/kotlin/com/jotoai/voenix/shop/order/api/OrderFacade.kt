package com.jotoai.voenix.shop.order.api

import com.jotoai.voenix.shop.order.api.dto.CreateOrderRequest
import com.jotoai.voenix.shop.order.api.dto.OrderDto
import java.util.UUID

/**
 * Main facade for Order module command operations.
 * This interface defines all administrative and user-facing operations for managing orders.
 */
interface OrderFacade {
    /**
     * Creates an order from the user's active cart.
     */
    fun createOrderFromCart(
        userId: Long,
        request: CreateOrderRequest,
    ): OrderDto

    /**
     * Cancels an order (if allowed).
     */
    fun cancelOrder(
        userId: Long,
        orderId: UUID,
    ): OrderDto
}
