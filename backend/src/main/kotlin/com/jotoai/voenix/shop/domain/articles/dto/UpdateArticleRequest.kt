package com.jotoai.voenix.shop.domain.articles.dto

import com.jotoai.voenix.shop.domain.articles.enums.FitType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class UpdateArticleRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,
    @field:NotBlank(message = "Short description is required")
    val descriptionShort: String,
    @field:NotBlank(message = "Long description is required")
    val descriptionLong: String,
    @field:NotBlank(message = "Example image filename is required")
    val exampleImageFilename: String,
    @field:NotNull(message = "Price is required")
    @field:Positive(message = "Price must be positive")
    val price: Int,
    val active: Boolean = true,
    @field:NotNull(message = "Category ID is required")
    val categoryId: Long,
    val subcategoryId: Long? = null,
    // Mug-specific details
    val mugDetails: UpdateMugDetailsRequest? = null,
    // Shirt-specific details
    val shirtDetails: UpdateShirtDetailsRequest? = null,
    // Pillow-specific details
    val pillowDetails: UpdatePillowDetailsRequest? = null,
)

data class UpdateMugDetailsRequest(
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
    val fillingQuantity: String? = null,
    val dishwasherSafe: Boolean = true,
)

data class UpdateShirtDetailsRequest(
    @field:NotBlank(message = "Material is required")
    val material: String,
    val careInstructions: String? = null,
    @field:NotNull(message = "Fit type is required")
    val fitType: FitType,
    @field:NotNull(message = "Available sizes are required")
    val availableSizes: List<String>,
)

data class UpdatePillowDetailsRequest(
    @field:NotNull(message = "Width is required")
    @field:Positive(message = "Width must be positive")
    val widthCm: Int,
    @field:NotNull(message = "Height is required")
    @field:Positive(message = "Height must be positive")
    val heightCm: Int,
    @field:NotNull(message = "Depth is required")
    @field:Positive(message = "Depth must be positive")
    val depthCm: Int,
    @field:NotBlank(message = "Material is required")
    val material: String,
    @field:NotBlank(message = "Filling type is required")
    val fillingType: String,
    val coverRemovable: Boolean = true,
    val washable: Boolean = true,
)
