package com.jotoai.voenix.shop.domain.prompts.dto

import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class UpdatePromptSlotVariantRequest(
    @field:Positive(message = "Prompt slot type ID must be positive")
    val promptSlotTypeId: Long? = null,
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String? = null,
    val prompt: String? = null,
    val description: String? = null,
    @field:Size(max = 500, message = "Example image filename must not exceed 500 characters")
    val exampleImageFilename: String? = null,
)
