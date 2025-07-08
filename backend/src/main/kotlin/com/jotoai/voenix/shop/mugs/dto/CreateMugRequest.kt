package com.jotoai.voenix.shop.mugs.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class CreateMugRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String,
    @field:NotBlank(message = "Long description is required")
    val descriptionLong: String,
    @field:NotBlank(message = "Short description is required")
    val descriptionShort: String,
    @field:NotBlank(message = "Image URL is required")
    @field:Size(max = 500, message = "Image URL must not exceed 500 characters")
    val image: String,
    @field:NotNull(message = "Price is required")
    @field:Min(value = 0, message = "Price must be greater than or equal to 0")
    val price: Int,
    @field:NotNull(message = "Height is required")
    @field:Positive(message = "Height must be positive")
    val heightMm: Int,
    @field:NotNull(message = "Diameter is required")
    @field:Positive(message = "Diameter must be positive")
    val diameterMm: Int,
    @field:NotNull(message = "Print template width is required")
    @field:Positive(message = "Print template width must be positive")
    val printTemplateWidthMm: Int,
    @field:NotNull(message = "Print template height is required")
    @field:Positive(message = "Print template height must be positive")
    val printTemplateHeightMm: Int,
    @field:Size(max = 255, message = "Filling quantity must not exceed 255 characters")
    val fillingQuantity: String? = null,
    val dishwasherSafe: Boolean = true,
    val active: Boolean = true,
)
