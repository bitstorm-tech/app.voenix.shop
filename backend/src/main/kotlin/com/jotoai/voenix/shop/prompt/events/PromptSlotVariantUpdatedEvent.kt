package com.jotoai.voenix.shop.prompt.events

import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.PromptSlotVariantDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a prompt slot variant is updated.
 */
@Externalized
data class PromptSlotVariantUpdatedEvent(
    val oldSlotVariant: PromptSlotVariantDto,
    val newSlotVariant: PromptSlotVariantDto,
)
