package com.jotoai.voenix.shop.openai.api.dto

import com.jotoai.voenix.shop.image.api.enums.ImageBackground
import com.jotoai.voenix.shop.image.api.enums.ImageQuality
import com.jotoai.voenix.shop.image.api.enums.ImageSize
import jakarta.validation.constraints.NotBlank

data class TestPromptRequest(
    @field:NotBlank(message = "Master prompt is required")
    val masterPrompt: String,
    val specificPrompt: String = "",
    val background: ImageBackground = ImageBackground.AUTO,
    val quality: ImageQuality = ImageQuality.LOW,
    val size: ImageSize = ImageSize.SQUARE_1024X1024,
)
