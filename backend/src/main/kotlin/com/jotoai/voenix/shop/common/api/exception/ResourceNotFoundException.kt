package com.jotoai.voenix.shop.common.api.exception

/**
 * Base exception for when a requested resource cannot be found.
 * This should be extended by modules for their specific resource not found scenarios.
 */
open class ResourceNotFoundException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(entity: String, property: String, value: Any) : super("$entity not found with $property: $value")
}
