package com.jotoai.voenix.shop.common.exception

class ResourceNotFoundException : RuntimeException {
    constructor(message: String) : super(message)
    
    constructor(resourceName: String, fieldName: String, fieldValue: Any) : 
        super("$resourceName not found with $fieldName: '$fieldValue'")
}