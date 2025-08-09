package com.jotoai.voenix.shop.order.api

import com.jotoai.voenix.shop.order.api.dto.OrderDto
import com.jotoai.voenix.shop.order.api.enums.OrderStatus
import java.util.UUID

/**
 * Administrative facade for Order module operations.
 * This interface defines all administrative operations that require elevated permissions.
 * All methods in this facade should be secured with appropriate authorization.
 */
interface AdminOrderFacade {
    /**
     * Updates an order status (administrative operation).
     * This operation should only be accessible by administrators.
     */
    fun updateOrderStatus(
        orderId: UUID,
        status: OrderStatus,
    ): OrderDto
}
