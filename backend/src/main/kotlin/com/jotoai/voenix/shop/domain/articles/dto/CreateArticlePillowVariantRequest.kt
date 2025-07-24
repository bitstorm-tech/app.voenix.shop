package com.jotoai.voenix.shop.domain.articles.dto

import jakarta.validation.constraints.NotBlank

data class CreateArticlePillowVariantRequest(
    @field:NotBlank(message = "Color is required")
    val color: String,
    @field:NotBlank(message = "Material is required")
    val material: String,
    val sku: String? = null,
    val exampleImageFilename: String? = null,
)
