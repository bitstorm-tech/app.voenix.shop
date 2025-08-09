package com.jotoai.voenix.shop.order.api.exception

/**
 * Exception thrown when attempting to create an order that already exists.
 * This exception is part of the public API for the Order module.
 */
class OrderAlreadyExistsException(
    val cartId: Long,
) : RuntimeException() {
    override val message: String =
        "Order already exists for cart: $cartId"
}
