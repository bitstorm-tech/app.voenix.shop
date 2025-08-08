package com.jotoai.voenix.shop.prompt.events

import com.jotoai.voenix.shop.prompt.api.dto.subcategories.PromptSubCategoryDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a new prompt subcategory is created.
 */
@Externalized
data class PromptSubCategoryCreatedEvent(
    val subCategory: PromptSubCategoryDto,
)
