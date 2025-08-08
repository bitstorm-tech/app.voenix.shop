package com.jotoai.voenix.shop.prompt.events

import com.jotoai.voenix.shop.prompt.api.dto.slottypes.PromptSlotTypeDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a prompt slot type is updated.
 */
@Externalized
data class PromptSlotTypeUpdatedEvent(
    val oldSlotType: PromptSlotTypeDto,
    val newSlotType: PromptSlotTypeDto,
)
