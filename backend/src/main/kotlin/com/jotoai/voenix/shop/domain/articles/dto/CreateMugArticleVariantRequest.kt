package com.jotoai.voenix.shop.domain.articles.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateMugArticleVariantRequest(
    @field:NotBlank(message = "Inside color code is required")
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Inside color code must be a valid hex color")
    val insideColorCode: String = "#ffffff",
    @field:NotBlank(message = "Outside color code is required")
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Outside color code must be a valid hex color")
    val outsideColorCode: String = "#ffffff",
    @field:NotBlank(message = "Name is required")
    val name: String,
    @field:Size(max = 100, message = "Supplier article number must not exceed 100 characters")
    val supplierArticleNumber: String? = null,
    val isDefault: Boolean = false,
)
