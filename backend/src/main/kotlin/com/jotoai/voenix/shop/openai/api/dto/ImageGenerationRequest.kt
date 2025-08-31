package com.jotoai.voenix.shop.openai.api.dto

import com.jotoai.voenix.shop.image.CropArea
import com.jotoai.voenix.shop.openai.api.enums.ImageBackground
import com.jotoai.voenix.shop.openai.api.enums.ImageQuality
import com.jotoai.voenix.shop.openai.api.enums.ImageSize
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
