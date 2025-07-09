package com.jotoai.voenix.shop.prompts.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Min

data class UpdatePromptSlotsRequest(
    @field:Valid
    val slots: List<PromptSlotRequest> = emptyList(),
)

data class PromptSlotRequest(
    val slotId: Long,
    @field:Min(value = 0, message = "Position must be non-negative")
    val position: Int = 0,
)
