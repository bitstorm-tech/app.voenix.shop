package com.jotoai.voenix.shop.prompt.events

import com.jotoai.voenix.shop.prompt.api.dto.subcategories.PromptSubCategoryDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a prompt subcategory is deleted.
 */
@Externalized
data class PromptSubCategoryDeletedEvent(
    val subCategory: PromptSubCategoryDto,
)
