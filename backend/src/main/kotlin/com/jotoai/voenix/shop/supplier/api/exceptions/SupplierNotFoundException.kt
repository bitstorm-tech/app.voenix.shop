package com.jotoai.voenix.shop.supplier.api.exceptions

/**
 * Exception thrown when a supplier is not found.
 *
 * This is a public API exception that can be thrown by any component
 * that needs to indicate a supplier was not found, typically in response
 * to queries with invalid supplier IDs or other identifying fields.
 *
 * @param message the detail message explaining why the supplier was not found
 */
class SupplierNotFoundException : RuntimeException {
    /**
     * Creates a new exception with a custom message.
     *
     * @param message the detail message
     */
    constructor(message: String) : super(message)

    /**
     * Creates a new exception with a standardized message format.
     *
     * @param resourceName the name of the resource (e.g., "Supplier")
     * @param fieldName the name of the field used for lookup (e.g., "id", "name")
     * @param fieldValue the value that was not found
     */
    constructor(resourceName: String, fieldName: String, fieldValue: Any) :
        super("$resourceName not found with $fieldName: '$fieldValue'")
}
