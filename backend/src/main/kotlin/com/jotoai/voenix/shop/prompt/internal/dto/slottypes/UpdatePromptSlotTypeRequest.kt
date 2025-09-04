package com.jotoai.voenix.shop.prompt.internal.dto.slottypes

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class UpdatePromptSlotTypeRequest(
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String? = null,
    @field:Min(value = 0, message = "Position must be a non-negative integer")
    val position: Int? = null,
)
