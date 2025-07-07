package com.jotoai.voenix.shop.mugs.dto

import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class UpdateMugSubCategoryRequest(
    @field:Positive(message = "Mug category ID must be positive")
    val mugCategoryId: Long?,
    
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String?,
    
    val description: String?
)