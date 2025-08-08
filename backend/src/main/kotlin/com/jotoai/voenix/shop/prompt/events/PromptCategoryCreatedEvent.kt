package com.jotoai.voenix.shop.prompt.events

import com.jotoai.voenix.shop.prompt.api.dto.categories.PromptCategoryDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a new prompt category is created.
 */
@Externalized
data class PromptCategoryCreatedEvent(
    val category: PromptCategoryDto,
)
