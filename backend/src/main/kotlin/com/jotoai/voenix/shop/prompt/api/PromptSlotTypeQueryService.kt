package com.jotoai.voenix.shop.prompt.api

import com.jotoai.voenix.shop.prompt.api.dto.slottypes.PromptSlotTypeDto

/**
 * Query service for Prompt Slot Type module read operations.
 * This interface defines all read-only operations for prompt slot type data.
 * It serves as the primary read API for other modules to access prompt slot type information.
 */
interface PromptSlotTypeQueryService {
    /**
     * Retrieves all prompt slot types.
     */
    fun getAllPromptSlotTypes(): List<PromptSlotTypeDto>

    /**
     * Retrieves a prompt slot type by its ID.
     * @param id The slot type ID
     * @return The prompt slot type
     * @throws RuntimeException if the slot type is not found
     */
    fun getPromptSlotTypeById(id: Long): PromptSlotTypeDto

    /**
     * Checks if a prompt slot type exists by its ID.
     * @param id The slot type ID
     * @return true if the slot type exists, false otherwise
     */
    fun existsById(id: Long): Boolean
}
