package com.jotoai.voenix.shop.domain.prompts.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreatePromptCategoryRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String,
)
