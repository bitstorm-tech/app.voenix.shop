package com.jotoai.voenix.shop.prompts.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreatePromptRequest(
    @field:NotBlank(message = "Title is required")
    @field:Size(max = 500, message = "Title must not exceed 500 characters")
    val title: String,
    val content: String? = null,
    val categoryId: Long? = null,
    val subcategoryId: Long? = null,
    val exampleImageFilename: String? = null,
    @field:Valid
    val slots: List<PromptSlotRequest> = emptyList(),
)
