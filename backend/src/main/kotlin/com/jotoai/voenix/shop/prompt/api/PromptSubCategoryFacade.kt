package com.jotoai.voenix.shop.prompt.api

import com.jotoai.voenix.shop.prompt.api.dto.subcategories.CreatePromptSubCategoryRequest
import com.jotoai.voenix.shop.prompt.api.dto.subcategories.PromptSubCategoryDto
import com.jotoai.voenix.shop.prompt.api.dto.subcategories.UpdatePromptSubCategoryRequest

/**
 * Main facade for Prompt SubCategory module write operations.
 * This interface defines all administrative operations for managing prompt subcategories.
 */
interface PromptSubCategoryFacade {
    /**
     * Creates a new prompt subcategory.
     */
    fun createPromptSubCategory(request: CreatePromptSubCategoryRequest): PromptSubCategoryDto

    /**
     * Updates an existing prompt subcategory.
     */
    fun updatePromptSubCategory(
        id: Long,
        request: UpdatePromptSubCategoryRequest,
    ): PromptSubCategoryDto

    /**
     * Deletes a prompt subcategory.
     */
    fun deletePromptSubCategory(id: Long)
}
