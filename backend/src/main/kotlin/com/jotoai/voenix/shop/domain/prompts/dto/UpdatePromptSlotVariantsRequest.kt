package com.jotoai.voenix.shop.domain.prompts.dto

import jakarta.validation.Valid

data class UpdatePromptSlotVariantsRequest(
    @field:Valid
    val slotVariants: List<PromptSlotVariantRequest> = emptyList(),
)

data class PromptSlotVariantRequest(
    val slotId: Long,
)
