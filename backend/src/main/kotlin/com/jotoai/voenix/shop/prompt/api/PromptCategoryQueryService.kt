package com.jotoai.voenix.shop.prompt.api

import com.jotoai.voenix.shop.prompt.api.dto.categories.PromptCategoryDto

/**
 * Query service for Prompt Category module read operations.
 * This interface defines all read-only operations for prompt category data.
 * It serves as the primary read API for other modules to access prompt category information.
 */
interface PromptCategoryQueryService {
    /**
     * Retrieves all prompt categories.
     */
    fun getAllPromptCategories(): List<PromptCategoryDto>

    /**
     * Checks if a prompt category exists by its ID.
     * @param id The category ID
     * @return true if the category exists, false otherwise
     */
    fun existsById(id: Long): Boolean
}
