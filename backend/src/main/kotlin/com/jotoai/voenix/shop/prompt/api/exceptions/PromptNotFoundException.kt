package com.jotoai.voenix.shop.prompt.api.exceptions

import com.jotoai.voenix.shop.application.ResourceNotFoundException

/**
 * Exception thrown when a prompt is not found.
 */
class PromptNotFoundException(
    entity: String,
    property: String,
    value: Any,
) : ResourceNotFoundException(entity, property, value)
