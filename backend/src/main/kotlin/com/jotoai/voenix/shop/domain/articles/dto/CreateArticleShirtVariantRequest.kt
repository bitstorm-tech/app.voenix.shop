package com.jotoai.voenix.shop.domain.articles.dto

import jakarta.validation.constraints.NotBlank

data class CreateArticleShirtVariantRequest(
    @field:NotBlank(message = "Color is required")
    val color: String,
    @field:NotBlank(message = "Size is required")
    val size: String,
    val exampleImageFilename: String? = null,
)
