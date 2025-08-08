package com.jotoai.voenix.shop.prompt.api

import com.jotoai.voenix.shop.prompt.api.dto.prompts.CreatePromptRequest
import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto
import com.jotoai.voenix.shop.prompt.api.dto.prompts.UpdatePromptRequest
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.AddSlotVariantsRequest
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.UpdatePromptSlotVariantsRequest

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

    /**
     * Adds slot variants to a prompt.
     */
    fun addSlotVariantsToPrompt(
        promptId: Long,
        request: AddSlotVariantsRequest,
    ): PromptDto

    /**
     * Updates the slot variants for a prompt (replaces existing).
     */
    fun updatePromptSlotVariants(
        promptId: Long,
        request: UpdatePromptSlotVariantsRequest,
    ): PromptDto

    /**
     * Removes a slot variant from a prompt.
     */
    fun removeSlotVariantFromPrompt(
        promptId: Long,
        slotId: Long,
    ): PromptDto
}
