package com.jotoai.voenix.shop.prompt.api

import com.jotoai.voenix.shop.prompt.api.dto.slottypes.CreatePromptSlotTypeRequest
import com.jotoai.voenix.shop.prompt.api.dto.slottypes.PromptSlotTypeDto
import com.jotoai.voenix.shop.prompt.api.dto.slottypes.UpdatePromptSlotTypeRequest

/**
 * Main facade for Prompt Slot Type module write operations.
 * This interface defines all administrative operations for managing prompt slot types.
 */
interface PromptSlotTypeFacade {
    /**
     * Creates a new prompt slot type.
     */
    fun createPromptSlotType(request: CreatePromptSlotTypeRequest): PromptSlotTypeDto

    /**
     * Updates an existing prompt slot type.
     */
    fun updatePromptSlotType(
        id: Long,
        request: UpdatePromptSlotTypeRequest,
    ): PromptSlotTypeDto

    /**
     * Deletes a prompt slot type.
     */
    fun deletePromptSlotType(id: Long)
}
