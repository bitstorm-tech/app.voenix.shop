package com.jotoai.voenix.shop.prompt.api

import com.jotoai.voenix.shop.prompt.api.dto.prompts.CreatePromptRequest
import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto
import com.jotoai.voenix.shop.prompt.api.dto.prompts.UpdatePromptRequest

/**
 * Main facade for Prompt module write operations.
 * This interface defines all administrative operations for managing prompts.
 */
interface PromptFacade {
    /**
     * Creates a new prompt.
     */
    fun createPrompt(request: CreatePromptRequest): PromptDto

    /**
     * Updates an existing prompt.
     */
    fun updatePrompt(
        id: Long,
        request: UpdatePromptRequest,
    ): PromptDto

    /**
     * Deletes a prompt.
     */
    fun deletePrompt(id: Long)
}
