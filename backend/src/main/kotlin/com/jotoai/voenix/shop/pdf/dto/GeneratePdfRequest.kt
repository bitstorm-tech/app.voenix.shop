package com.jotoai.voenix.shop.pdf.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class GeneratePdfRequest(
    @field:NotNull(message = "Mug ID is required")
    val mugId: Long,
    @field:NotBlank(message = "Image filename is required")
    val imageFilename: String,
)
