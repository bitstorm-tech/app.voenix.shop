package com.jotoai.voenix.shop.vat.internal.exception

/**
 * Exception thrown when a VAT entity cannot be found.
 * Scoped to the VAT module internals.
 */
class VatNotFoundException(
    entityName: String,
    fieldName: String,
    fieldValue: Any?,
) : RuntimeException() {
    override val message: String =
        "Could not find $entityName with $fieldName = $fieldValue"
}
