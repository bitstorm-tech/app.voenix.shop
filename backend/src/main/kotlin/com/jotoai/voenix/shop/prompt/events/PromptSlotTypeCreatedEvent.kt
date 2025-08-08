package com.jotoai.voenix.shop.prompt.events

import com.jotoai.voenix.shop.prompt.api.dto.slottypes.PromptSlotTypeDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a new prompt slot type is created.
 */
@Externalized
data class PromptSlotTypeCreatedEvent(
    val slotType: PromptSlotTypeDto,
)
