package com.jotoai.voenix.shop.openai.internal.provider

import com.jotoai.voenix.shop.openai.internal.dto.ImageEditBytesResponse
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptRequest
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptResponse
import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto
import org.springframework.web.multipart.MultipartFile

interface ImageGenerationProvider {
    suspend fun generateImages(
        imageFile: MultipartFile,
        prompt: PromptDto,
        options: GenerationOptions,
    ): ImageEditBytesResponse

    suspend fun testPrompt(
        imageFile: MultipartFile,
        request: TestPromptRequest,
    ): TestPromptResponse
}

data class GenerationOptions(
    val size: String,
    val background: String? = null,
    val quality: String? = null,
    val n: Int = 4,
)
