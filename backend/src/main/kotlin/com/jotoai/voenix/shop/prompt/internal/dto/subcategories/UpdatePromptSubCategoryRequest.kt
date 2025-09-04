package com.jotoai.voenix.shop.prompt.internal.dto.subcategories

import jakarta.validation.constraints.Size

data class UpdatePromptSubCategoryRequest(
    val promptCategoryId: Long? = null,
    @field:Size(max = 255, message = "Name cannot exceed 255 characters")
    val name: String? = null,
    @field:Size(max = 1000, message = "Description cannot exceed 1000 characters")
    val description: String? = null,
)
