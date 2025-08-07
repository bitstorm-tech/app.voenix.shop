package com.jotoai.voenix.shop.image.api.dto

import com.jotoai.voenix.shop.domain.openai.dto.enums.ImageBackground
import com.jotoai.voenix.shop.domain.openai.dto.enums.ImageQuality
import com.jotoai.voenix.shop.domain.openai.dto.enums.ImageSize
import jakarta.validation.constraints.NotNull

data class PublicImageGenerationRequest(
    @field:NotNull(message = "Prompt ID is required")
    val promptId: Long,
    val background: ImageBackground = ImageBackground.AUTO,
    val quality: ImageQuality = ImageQuality.LOW,
    val size: ImageSize = ImageSize.LANDSCAPE_1536X1024,
    val n: Int = 1,
)
