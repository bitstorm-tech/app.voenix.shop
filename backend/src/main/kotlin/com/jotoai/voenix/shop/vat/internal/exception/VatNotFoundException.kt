package com.jotoai.voenix.shop.vat.internal.exception

/**
 * Exception thrown when a VAT configuration is not found.
 * This is an internal exception for the VAT module.
 */
class VatNotFoundException : RuntimeException {
    constructor(message: String) : super(message)

    constructor(resourceName: String, fieldName: String, fieldValue: Any) :
        super("$resourceName not found with $fieldName: '$fieldValue'")
}
