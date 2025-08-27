package com.jotoai.voenix.shop.prompt.api

import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.PromptSlotVariantDto

/**
 * Query service for Prompt Slot Variant module read operations.
 * This interface defines all read-only operations for prompt slot variant data.
 * It serves as the primary read API for other modules to access prompt slot variant information.
 */
interface PromptSlotVariantQueryService {
    /**
     * Retrieves all slot variants.
     */
    fun getAllSlotVariants(): List<PromptSlotVariantDto>

    /**
     * Retrieves a slot variant by its ID.
     * @param id The slot variant ID
     * @return The prompt slot variant
     * @throws RuntimeException if the slot variant is not found
     */
    fun getSlotVariantById(id: Long): PromptSlotVariantDto

    /**
     * Checks if a slot variant exists by its ID.
     * @param id The slot variant ID
     * @return true if the slot variant exists, false otherwise
     */
    fun existsById(id: Long): Boolean
}
