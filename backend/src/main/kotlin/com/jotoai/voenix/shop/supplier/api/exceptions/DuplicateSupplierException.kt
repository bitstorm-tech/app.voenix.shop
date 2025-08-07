package com.jotoai.voenix.shop.supplier.api.exceptions

/**
 * Exception thrown when attempting to create or update a supplier with duplicate values
 * for fields that must be unique (e.g., name, email).
 *
 * This exception indicates a business rule violation where uniqueness constraints
 * are not satisfied, typically resulting in a 409 Conflict HTTP status.
 *
 * @param message the detail message explaining the duplicate constraint violation
 * @param cause the underlying cause of the exception (optional)
 */
class DuplicateSupplierException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    /**
     * Creates a duplicate supplier exception for a specific field.
     *
     * @param fieldName the name of the field that has a duplicate value
     * @param fieldValue the duplicate value
     */
    constructor(fieldName: String, fieldValue: String) :
        this("Supplier with $fieldName '$fieldValue' already exists")
}
