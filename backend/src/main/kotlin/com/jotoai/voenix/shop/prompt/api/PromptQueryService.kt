package com.jotoai.voenix.shop.prompt.api

import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto
import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptSummaryDto
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
     * Checks if a prompt exists by its ID.
     * @param id The prompt ID
     * @return true if the prompt exists, false otherwise
     */
    fun existsById(id: Long): Boolean

    /**
     * Retrieves all active public prompts.
     * @return List of public prompts suitable for public API consumption
     */
    fun getAllPublicPrompts(): List<PublicPromptDto>

    /**
     * Retrieves prompt summaries by their IDs.
     * @param ids The list of prompt IDs to retrieve
     * @return Map of prompt ID to prompt summary, only including found prompts
     */
    fun getPromptSummariesByIds(ids: List<Long>): List<PromptSummaryDto>
}
