package com.jotoai.voenix.shop.prompts.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class CreatePromptSubCategoryRequest(
    @field:NotNull(message = "Category ID is required")
    val promptCategoryId: Long,
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name cannot exceed 255 characters")
    val name: String,
    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String? = null,
)
