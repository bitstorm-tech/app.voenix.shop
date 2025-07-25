package com.jotoai.voenix.shop.domain.articles.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class CreateArticleMugVariantRequest(
    @field:NotBlank(message = "Inside color code is required")
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Inside color code must be a valid hex color")
    val insideColorCode: String = "#ffffff",
    @field:NotBlank(message = "Outside color code is required")
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Outside color code must be a valid hex color")
    val outsideColorCode: String = "#ffffff",
    @field:NotBlank(message = "Name is required")
    val name: String,
    val exampleImageFilename: String? = null,
)
