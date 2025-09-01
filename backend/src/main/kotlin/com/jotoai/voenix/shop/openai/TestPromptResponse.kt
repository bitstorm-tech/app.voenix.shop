package com.jotoai.voenix.shop.openai

data class TestPromptResponse(
    val imageUrl: String,
    val requestParams: TestPromptRequestParams,
)

data class TestPromptRequestParams(
    val model: String,
    val size: String,
    val n: Int,
    val responseFormat: String,
    val masterPrompt: String,
    val specificPrompt: String,
    val combinedPrompt: String,
    val quality: String?,
    val background: String?,
)
