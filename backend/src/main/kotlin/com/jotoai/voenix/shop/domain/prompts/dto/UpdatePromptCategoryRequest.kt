package com.jotoai.voenix.shop.domain.prompts.dto

import jakarta.validation.constraints.Size

data class UpdatePromptCategoryRequest(
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String? = null,
)
