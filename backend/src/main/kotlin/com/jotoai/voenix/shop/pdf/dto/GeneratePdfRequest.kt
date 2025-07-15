package com.jotoai.voenix.shop.pdf.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class GeneratePdfRequest(
    @field:NotBlank(message = "QR code content is required")
    val qrContent: String,
    @field:Positive(message = "Image width must be positive")
    val imageWidth: Float,
    @field:Positive(message = "Image height must be positive")
    val imageHeight: Float,
)
