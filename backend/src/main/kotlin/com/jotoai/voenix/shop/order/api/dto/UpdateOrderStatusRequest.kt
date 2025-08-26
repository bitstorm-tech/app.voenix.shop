package com.jotoai.voenix.shop.order.api.dto

import com.jotoai.voenix.shop.order.api.enums.OrderStatus

/**
 * Request DTO for updating order status.
 */
data class UpdateOrderStatusRequest(
    val status: OrderStatus,
)