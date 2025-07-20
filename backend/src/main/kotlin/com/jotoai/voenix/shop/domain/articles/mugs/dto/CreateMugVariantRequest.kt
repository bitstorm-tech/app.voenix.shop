package com.jotoai.voenix.shop.domain.articles.mugs.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateMugVariantRequest(
    @field:NotNull(message = "Mug ID is required")
    val mugId: Long,
    @field:NotBlank(message = "Color code is required")
    val colorCode: String,
    @field:NotBlank(message = "Example image filename is required")
    val exampleImageFilename: String,
)
