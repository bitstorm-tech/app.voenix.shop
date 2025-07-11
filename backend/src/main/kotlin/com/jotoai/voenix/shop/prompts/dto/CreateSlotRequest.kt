package com.jotoai.voenix.shop.prompts.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class CreateSlotRequest(
    @field:NotNull(message = "Slot type ID is required")
    @field:Positive(message = "Slot type ID must be positive")
    val slotTypeId: Long,
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String,
    @field:NotBlank(message = "Prompt is required")
    val prompt: String,
    val description: String? = null,
)
