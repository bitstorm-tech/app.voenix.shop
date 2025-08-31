package com.jotoai.voenix.shop.prompt.api.exceptions

import com.jotoai.voenix.shop.application.ResourceNotFoundException

/**
 * Exception thrown when a prompt category is not found.
 */
class PromptCategoryNotFoundException(
    entity: String,
    property: String,
    value: Any,
) : ResourceNotFoundException(entity, property, value)
