package com.jotoai.voenix.shop.prompt.internal.dto.slottypes

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class CreatePromptSlotTypeRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String,
    @field:NotNull(message = "Position is required")
    @field:Min(value = 0, message = "Position must be a non-negative integer")
    val position: Int,
)
