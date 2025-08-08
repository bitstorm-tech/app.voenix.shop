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
     * Retrieves a prompt category by its ID.
     * @param id The category ID
     * @return The prompt category
     * @throws RuntimeException if the category is not found
     */
    fun getPromptCategoryById(id: Long): PromptCategoryDto

    /**
     * Searches prompt categories by name (case-insensitive partial match).
     * @param name The name search term
     * @return List of matching categories
     */
    fun searchPromptCategoriesByName(name: String): List<PromptCategoryDto>

    /**
     * Checks if a prompt category exists by its ID.
     * @param id The category ID
     * @return true if the category exists, false otherwise
     */
    fun existsById(id: Long): Boolean
}
