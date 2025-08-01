package com.jotoai.voenix.shop.domain.openai.service

import com.jotoai.voenix.shop.domain.openai.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.domain.openai.dto.ImageEditBytesResponse
import com.jotoai.voenix.shop.domain.openai.dto.TestPromptRequest
import com.jotoai.voenix.shop.domain.openai.dto.TestPromptResponse
import org.springframework.web.multipart.MultipartFile

/**
 * Strategy interface for image generation implementations.
 * Allows switching between different image generation backends (OpenAI, test mode, etc.)
 */
interface ImageGenerationStrategy {
    /**
     * Generates edited images based on the provided request and returns raw image bytes.
     * This allows the caller to handle storage using their preferred strategy.
     */
    fun generateImages(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
    ): ImageEditBytesResponse

    /**
     * Tests a prompt with the image generation service and returns the results.
     */
    fun testPrompt(
        imageFile: MultipartFile,
        request: TestPromptRequest,
    ): TestPromptResponse
}
