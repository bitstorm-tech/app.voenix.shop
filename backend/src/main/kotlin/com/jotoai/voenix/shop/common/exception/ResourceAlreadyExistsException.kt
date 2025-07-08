package com.jotoai.voenix.shop.common.exception

class ResourceAlreadyExistsException : RuntimeException {
    constructor(message: String) : super(message)

    constructor(resourceName: String, fieldName: String, fieldValue: Any) :
        super("$resourceName already exists with $fieldName: '$fieldValue'")
}
