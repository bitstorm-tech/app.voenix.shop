package com.jotoai.voenix.shop.prompt.events

import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.PromptSlotVariantDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a new prompt slot variant is created.
 */
@Externalized
data class PromptSlotVariantCreatedEvent(
    val slotVariant: PromptSlotVariantDto,
)
