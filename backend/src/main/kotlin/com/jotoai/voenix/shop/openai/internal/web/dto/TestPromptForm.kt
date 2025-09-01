package com.jotoai.voenix.shop.openai.internal.web.dto

import org.springframework.web.multipart.MultipartFile

data class TestPromptForm(
    val image: MultipartFile,
    val masterPrompt: String,
    val specificPrompt: String? = null,
    val background: String,
    val quality: String,
    val size: String,
)
