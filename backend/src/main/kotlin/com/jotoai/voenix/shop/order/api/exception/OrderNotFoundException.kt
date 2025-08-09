package com.jotoai.voenix.shop.order.api.exception

/**
 * Exception thrown when an order entity cannot be found.
 * This exception is part of the public API for the Order module.
 */
class OrderNotFoundException(
    val entityName: String,
    val fieldName: String,
    val fieldValue: Any?,
) : RuntimeException() {
    override val message: String =
        "Could not find $entityName with $fieldName = $fieldValue"
}
