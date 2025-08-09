package com.jotoai.voenix.shop.order.api.exception

import com.jotoai.voenix.shop.order.api.enums.OrderStatus

/**
 * Exception thrown when an order cannot be cancelled due to its current status.
 * This exception is part of the public API for the Order module.
 */
class OrderCannotBeCancelledException(
    val orderId: String,
    val currentStatus: OrderStatus,
) : RuntimeException() {
    override val message: String =
        "Order $orderId cannot be cancelled in current status: $currentStatus"
}
