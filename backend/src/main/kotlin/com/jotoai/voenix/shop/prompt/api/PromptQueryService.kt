package com.jotoai.voenix.shop.prompt.api

import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto
import com.jotoai.voenix.shop.prompt.api.dto.pub.PublicPromptDto

/**
 * Query service for Prompt module read operations.
 * This interface defines all read-only operations for prompt data.
 * It serves as the primary read API for other modules to access prompt information.
 */
interface PromptQueryService {
    /**
     * Retrieves all prompts.
     */
    fun getAllPrompts(): List<PromptDto>

    /**
     * Retrieves a prompt by its ID.
     * @param id The prompt ID
     * @return The prompt
     * @throws RuntimeException if the prompt is not found
     */
    fun getPromptById(id: Long): PromptDto

    /**
     * Searches prompts by title (case-insensitive partial match).
     * @param title The title search term
     * @return List of matching prompts
     */
    fun searchPromptsByTitle(title: String): List<PromptDto>

    /**
     * Retrieves all active prompts for public use.
     */
    fun getAllActivePrompts(): List<PublicPromptDto>

    /**
     * Checks if a prompt exists by its ID.
     * @param id The prompt ID
     * @return true if the prompt exists, false otherwise
     */
    fun existsById(id: Long): Boolean
}
