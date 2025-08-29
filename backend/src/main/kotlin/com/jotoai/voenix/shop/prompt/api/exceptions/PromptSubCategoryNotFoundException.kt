package com.jotoai.voenix.shop.prompt.api.exceptions

import com.jotoai.voenix.shop.application.api.exception.ResourceNotFoundException

/**
 * Exception thrown when a prompt subcategory is not found.
 */
class PromptSubCategoryNotFoundException(
    entity: String,
    property: String,
    value: Any,
) : ResourceNotFoundException(entity, property, value)
