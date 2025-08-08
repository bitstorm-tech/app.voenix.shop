package com.jotoai.voenix.shop.prompt.api.dto.slotvariants

import jakarta.validation.Valid

data class UpdatePromptSlotVariantsRequest(
    @field:Valid
    val slotVariants: List<PromptSlotVariantRequest> = emptyList(),
)

data class PromptSlotVariantRequest(
    val slotId: Long,
)
