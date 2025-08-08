package com.jotoai.voenix.shop.prompt.api

import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.CreatePromptSlotVariantRequest
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.PromptSlotVariantDto
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.UpdatePromptSlotVariantRequest

/**
 * Main facade for Prompt Slot Variant module write operations.
 * This interface defines all administrative operations for managing prompt slot variants.
 */
interface PromptSlotVariantFacade {
    /**
     * Creates a new prompt slot variant.
     */
    fun createSlotVariant(request: CreatePromptSlotVariantRequest): PromptSlotVariantDto

    /**
     * Updates an existing prompt slot variant.
     */
    fun updateSlotVariant(
        id: Long,
        request: UpdatePromptSlotVariantRequest,
    ): PromptSlotVariantDto

    /**
     * Deletes a prompt slot variant.
     */
    fun deleteSlotVariant(id: Long)
}
