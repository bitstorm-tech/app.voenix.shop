package com.jotoai.voenix.shop.country.api.exceptions

/**
 * Exception thrown when a country is not found.
 * This exception is part of the Country module's public API.
 */
class CountryNotFoundException : RuntimeException {
    constructor(message: String) : super(message)

    constructor(resourceName: String, fieldName: String, fieldValue: Any) :
        super("$resourceName not found with $fieldName: '$fieldValue'")
}
