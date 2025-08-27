package com.jotoai.voenix.shop.prompt.api

import com.jotoai.voenix.shop.prompt.api.dto.subcategories.PromptSubCategoryDto

/**
 * Query service for Prompt SubCategory module read operations.
 * This interface defines all read-only operations for prompt subcategory data.
 * It serves as the primary read API for other modules to access prompt subcategory information.
 */
interface PromptSubCategoryQueryService {
    /**
     * Retrieves all prompt subcategories.
     */
    fun getAllPromptSubCategories(): List<PromptSubCategoryDto>

    /**
     * Retrieves prompt subcategories by category ID.
     * @param categoryId The category ID
     * @return List of subcategories for the category
     */
    fun getPromptSubCategoriesByCategory(categoryId: Long): List<PromptSubCategoryDto>


    /**
     * Checks if a prompt subcategory exists by its ID.
     * @param id The subcategory ID
     * @return true if the subcategory exists, false otherwise
     */
    fun existsById(id: Long): Boolean
}
