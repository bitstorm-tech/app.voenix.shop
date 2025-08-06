package com.jotoai.voenix.shop.country.internal.exception

/**
 * Exception thrown when a country is not found.
 * This is an internal exception for the Country module.
 */
class CountryNotFoundException : RuntimeException {
    constructor(message: String) : super(message)

    constructor(resourceName: String, fieldName: String, fieldValue: Any) :
        super("$resourceName not found with $fieldName: '$fieldValue'")
}
