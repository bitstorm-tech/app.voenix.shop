package com.jotoai.voenix.shop.mugs.dto

import jakarta.validation.constraints.Size

data class UpdateMugCategoryRequest(
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String?,
    
    val description: String?
)