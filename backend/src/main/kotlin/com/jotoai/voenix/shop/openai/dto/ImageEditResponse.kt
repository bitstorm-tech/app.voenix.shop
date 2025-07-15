package com.jotoai.voenix.shop.openai.dto

data class ImageEditResponse(
    val images: List<GeneratedImage>,
)

data class GeneratedImage(
    val url: String,
    val revisedPrompt: String? = null,
)
