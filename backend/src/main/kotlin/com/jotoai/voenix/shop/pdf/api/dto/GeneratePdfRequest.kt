package com.jotoai.voenix.shop.pdf.api.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class GeneratePdfRequest(
    @field:NotNull(message = "Article ID is required")
    val articleId: Long,
    @field:NotBlank(message = "Image filename is required")
    val imageFilename: String,
)
