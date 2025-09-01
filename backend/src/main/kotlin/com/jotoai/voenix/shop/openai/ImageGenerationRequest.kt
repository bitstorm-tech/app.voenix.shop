package com.jotoai.voenix.shop.openai

import com.jotoai.voenix.shop.image.CropArea
import jakarta.validation.constraints.NotNull

data class ImageGenerationRequest(
    @field:NotNull(message = "Prompt ID is required")
    val promptId: Long,
    val background: ImageBackground = ImageBackground.AUTO,
    val quality: ImageQuality = ImageQuality.LOW,
    val size: ImageSize = ImageSize.LANDSCAPE_1536X1024,
    val n: Int = 1,
    val cropArea: CropArea? = null,
)
