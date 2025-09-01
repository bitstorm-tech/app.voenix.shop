package com.jotoai.voenix.shop.openai

import jakarta.validation.constraints.NotBlank

data class TestPromptRequest(
    @field:NotBlank(message = "Master prompt is required")
    val masterPrompt: String,
    val specificPrompt: String = "",
    val backgroundString: String = "AUTO",
    val qualityString: String = "LOW",
    val sizeString: String = "1024x1024",
) {
    fun getBackground(): ImageBackground =
        when (backgroundString.uppercase()) {
            "TRANSPARENT" -> ImageBackground.TRANSPARENT
            "OPAQUE" -> ImageBackground.OPAQUE
            "AUTO" -> ImageBackground.AUTO
            else -> ImageBackground.AUTO
        }

    fun getQuality(): ImageQuality =
        when (qualityString.uppercase()) {
            "LOW" -> ImageQuality.LOW
            "MEDIUM" -> ImageQuality.MEDIUM
            "HIGH" -> ImageQuality.HIGH
            else -> ImageQuality.LOW
        }

    fun getSize(): ImageSize =
        when (sizeString) {
            "1024x1024" -> ImageSize.SQUARE_1024X1024
            "1536x1024" -> ImageSize.LANDSCAPE_1536X1024
            "1024x1536" -> ImageSize.PORTRAIT_1024X1536
            else -> ImageSize.SQUARE_1024X1024
        }
}
