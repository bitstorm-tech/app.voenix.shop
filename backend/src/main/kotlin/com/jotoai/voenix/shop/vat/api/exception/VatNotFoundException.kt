package com.jotoai.voenix.shop.vat.api.exception

/**
 * Exception thrown when a VAT entity cannot be found.
 * This exception is part of the public API for the VAT module.
 */
class VatNotFoundException(
    val entityName: String,
    val fieldName: String,
    val fieldValue: Any?,
) : RuntimeException() {
    override val message: String =
        "Could not find $entityName with $fieldName = $fieldValue"
}
