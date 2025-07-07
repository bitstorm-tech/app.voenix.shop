package com.jotoai.voenix.shop.mugs.dto

import jakarta.validation.constraints.*

data class UpdateMugRequest(
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String? = null,
    
    val descriptionLong: String? = null,
    
    val descriptionShort: String? = null,
    
    @field:Size(max = 500, message = "Image URL must not exceed 500 characters")
    val image: String? = null,
    
    @field:Min(value = 0, message = "Price must be greater than or equal to 0")
    val price: Int? = null,
    
    @field:Positive(message = "Height must be positive")
    val heightMm: Int? = null,
    
    @field:Positive(message = "Diameter must be positive")
    val diameterMm: Int? = null,
    
    @field:Positive(message = "Print template width must be positive")
    val printTemplateWidthMm: Int? = null,
    
    @field:Positive(message = "Print template height must be positive")
    val printTemplateHeightMm: Int? = null,
    
    @field:Size(max = 255, message = "Filling quantity must not exceed 255 characters")
    val fillingQuantity: String? = null,
    
    val dishwasherSafe: Boolean? = null,
    
    val active: Boolean? = null
)