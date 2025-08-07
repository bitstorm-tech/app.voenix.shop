package com.jotoai.voenix.shop.supplier.api.exceptions

/**
 * Exception thrown when supplier data fails validation rules or contains invalid values.
 *
 * This exception indicates that the provided supplier data does not meet business
 * requirements or constraints, typically resulting in a 400 Bad Request HTTP status.
 *
 * @param message the detail message explaining what data is invalid
 * @param cause the underlying cause of the exception (optional)
 */
class InvalidSupplierDataException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    /**
     * Creates an invalid data exception for a specific field.
     *
     * @param fieldName the name of the invalid field
     * @param fieldValue the invalid value
     * @param reason optional reason why the value is invalid
     */
    constructor(fieldName: String, fieldValue: Any?, reason: String = "is invalid") :
        this("Field $fieldName with value '$fieldValue' $reason")
}
