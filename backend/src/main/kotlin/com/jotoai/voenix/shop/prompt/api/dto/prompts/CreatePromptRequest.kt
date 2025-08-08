package com.jotoai.voenix.shop.prompt.api.dto.prompts

import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.PromptSlotVariantRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreatePromptRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 500, message = "Title must not exceed 500 characters")
    val title: String,
    val promptText: String? = null,
    val categoryId: Long? = null,
    val subcategoryId: Long? = null,
    val exampleImageFilename: String? = null,
    @field:Valid
    val slots: List<PromptSlotVariantRequest> = emptyList(),
)
