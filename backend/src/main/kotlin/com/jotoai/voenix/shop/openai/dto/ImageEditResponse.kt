package com.jotoai.voenix.shop.openai.dto

data class ImageEditResponse(
    val images: List<GeneratedImage>,
)

data class GeneratedImage(
    val url: String? = null,
    val b64Json: String? = null,
    val revisedPrompt: String? = null,
)
