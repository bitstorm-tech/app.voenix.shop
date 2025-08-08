package com.jotoai.voenix.shop.prompt.api

import com.jotoai.voenix.shop.prompt.api.dto.categories.CreatePromptCategoryRequest
import com.jotoai.voenix.shop.prompt.api.dto.categories.PromptCategoryDto
import com.jotoai.voenix.shop.prompt.api.dto.categories.UpdatePromptCategoryRequest

/**
 * Main facade for Prompt Category module write operations.
 * This interface defines all administrative operations for managing prompt categories.
 */
interface PromptCategoryFacade {
    /**
     * Creates a new prompt category.
     */
    fun createPromptCategory(request: CreatePromptCategoryRequest): PromptCategoryDto

    /**
     * Updates an existing prompt category.
     */
    fun updatePromptCategory(
        id: Long,
        request: UpdatePromptCategoryRequest,
    ): PromptCategoryDto

    /**
     * Deletes a prompt category.
     */
    fun deletePromptCategory(id: Long)
}
