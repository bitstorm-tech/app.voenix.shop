package com.jotoai.voenix.shop.domain.pdf.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class PublicPdfGenerationRequest(
    @field:NotNull(message = "Mug ID is required")
    val mugId: Long,
    @field:NotBlank(message = "Image URL is required")
    val imageUrl: String,
)
