package com.jotoai.voenix.shop.supplier.internal.exception

/**
 * Exception thrown when a supplier is not found.
 * This is an internal exception for the supplier module.
 */
class SupplierNotFoundException : RuntimeException {
    constructor(message: String) : super(message)

    constructor(resourceName: String, fieldName: String, fieldValue: Any) :
        super("$resourceName not found with $fieldName: '$fieldValue'")
}
