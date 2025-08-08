package com.jotoai.voenix.shop.prompt.events

import com.jotoai.voenix.shop.prompt.api.dto.categories.PromptCategoryDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a prompt category is updated.
 */
@Externalized
data class PromptCategoryUpdatedEvent(
    val oldCategory: PromptCategoryDto,
    val newCategory: PromptCategoryDto,
)
