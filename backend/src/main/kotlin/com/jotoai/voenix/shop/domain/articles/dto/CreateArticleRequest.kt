package com.jotoai.voenix.shop.domain.articles.dto

import com.jotoai.voenix.shop.domain.articles.enums.ArticleType
import com.jotoai.voenix.shop.domain.articles.enums.FitType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class CreateArticleRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,
    @field:NotBlank(message = "Short description is required")
    val descriptionShort: String,
    @field:NotBlank(message = "Long description is required")
    val descriptionLong: String,
    val active: Boolean = true,
    @field:NotNull(message = "Article type is required")
    val articleType: ArticleType,
    @field:NotNull(message = "Category ID is required")
    val categoryId: Long,
    val subcategoryId: Long? = null,
    val supplierId: Long? = null,
    val mugVariants: List<CreateArticleMugVariantRequest>? = null,
    val shirtVariants: List<CreateArticleShirtVariantRequest>? = null,
    // Mug-specific details
    val mugDetails: CreateMugDetailsRequest? = null,
    // Shirt-specific details
    val shirtDetails: CreateShirtDetailsRequest? = null,
)

data class CreateMugDetailsRequest(
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

data class CreateShirtDetailsRequest(
    @field:NotBlank(message = "Material is required")
    val material: String,
    val careInstructions: String? = null,
    @field:NotNull(message = "Fit type is required")
    val fitType: FitType,
    @field:NotNull(message = "Available sizes are required")
    val availableSizes: List<String>,
)
