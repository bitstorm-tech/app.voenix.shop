package com.jotoai.voenix.shop.prompts.dto

import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class UpdateSlotRequest(
    @field:Positive(message = "Slot type ID must be positive")
    val slotTypeId: Long? = null,
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String? = null,
    val prompt: String? = null,
    val description: String? = null,
    @field:Size(max = 500, message = "Example image filename must not exceed 500 characters")
    val exampleImageFilename: String? = null,
)
