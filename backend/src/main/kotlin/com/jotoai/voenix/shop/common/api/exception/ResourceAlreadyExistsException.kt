package com.jotoai.voenix.shop.common.api.exception

/**
 * Base exception for when attempting to create a resource that already exists.
 * This should be extended by modules for their specific duplicate resource scenarios.
 */
open class ResourceAlreadyExistsException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(entity: String, property: String, value: Any) : super("$entity already exists with $property: $value")
}
