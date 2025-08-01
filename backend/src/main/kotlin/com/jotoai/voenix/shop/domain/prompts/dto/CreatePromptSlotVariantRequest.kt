package com.jotoai.voenix.shop.domain.prompts.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class CreatePromptSlotVariantRequest(
    @field:NotNull(message = "Prompt slot type ID is required")
    @field:Positive(message = "Prompt slot type ID must be positive")
    val promptSlotTypeId: Long,
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String,
    val prompt: String? = null,
    val description: String? = null,
    @field:Size(max = 500, message = "Example image filename must not exceed 500 characters")
    val exampleImageFilename: String? = null,
)
