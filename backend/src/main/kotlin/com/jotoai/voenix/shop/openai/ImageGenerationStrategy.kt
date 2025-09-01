package com.jotoai.voenix.shop.openai

import org.springframework.web.multipart.MultipartFile

/**
 * Internal strategy interface for image generation.
 * This interface is used within the OpenAI module to switch between different
 * image generation implementations (test mode vs. production mode).
 */
interface ImageGenerationStrategy {
    fun generateImages(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
    ): ImageEditBytesResponse

    fun testPrompt(
        imageFile: MultipartFile,
        request: TestPromptRequest,
    ): TestPromptResponse
}
