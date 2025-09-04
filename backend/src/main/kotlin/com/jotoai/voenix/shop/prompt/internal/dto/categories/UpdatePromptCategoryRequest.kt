package com.jotoai.voenix.shop.prompt.internal.dto.categories

import jakarta.validation.constraints.Size

data class UpdatePromptCategoryRequest(
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String? = null,
)
