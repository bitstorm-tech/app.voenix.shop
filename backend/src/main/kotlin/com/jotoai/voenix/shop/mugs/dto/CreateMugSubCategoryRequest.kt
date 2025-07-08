package com.jotoai.voenix.shop.mugs.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class CreateMugSubCategoryRequest(
    @field:NotNull(message = "Mug category ID is required")
    @field:Positive(message = "Mug category ID must be positive")
    val mugCategoryId: Long,
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String,
    val description: String?,
)
