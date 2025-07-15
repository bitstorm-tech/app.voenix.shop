package com.jotoai.voenix.shop.prompts.dto

import jakarta.validation.Valid

data class UpdatePromptSlotsRequest(
    @field:Valid
    val slots: List<PromptSlotRequest> = emptyList(),
)

data class PromptSlotRequest(
    val slotId: Long,
)
