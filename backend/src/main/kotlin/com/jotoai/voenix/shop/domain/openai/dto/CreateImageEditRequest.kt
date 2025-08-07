package com.jotoai.voenix.shop.domain.openai.dto

import com.jotoai.voenix.shop.image.api.enums.ImageBackground
import com.jotoai.voenix.shop.image.api.enums.ImageQuality
import com.jotoai.voenix.shop.image.api.enums.ImageSize
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class CreateImageEditRequest(
    @field:NotNull(message = "Prompt ID is required")
    val promptId: Long,
    @field:NotNull(message = "Background is required")
    val background: ImageBackground = ImageBackground.AUTO,
    @field:NotNull(message = "Quality is required")
    val quality: ImageQuality = ImageQuality.LOW,
    @field:NotNull(message = "Size is required")
    val size: ImageSize = ImageSize.LANDSCAPE_1536X1024,
    @field:Min(value = 1, message = "Number of images must be at least 1")
    @field:Max(value = 10, message = "Number of images cannot exceed 10")
    val n: Int = 1,
)
