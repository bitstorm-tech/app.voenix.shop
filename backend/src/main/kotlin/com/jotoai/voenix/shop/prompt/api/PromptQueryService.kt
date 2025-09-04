package com.jotoai.voenix.shop.prompt.api

import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto

/**
 * Query service for Prompt module read operations.
 * This interface defines all read-only operations for prompt data.
 * It serves as the primary read API for other modules to access prompt information.
 */
interface PromptQueryService {
    fun getPromptById(id: Long): PromptDto

    fun existsById(id: Long): Boolean
}
