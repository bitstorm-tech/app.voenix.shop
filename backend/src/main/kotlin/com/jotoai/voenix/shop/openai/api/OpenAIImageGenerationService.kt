package com.jotoai.voenix.shop.openai.api

import com.jotoai.voenix.shop.openai.api.dto.ImageGenerationRequest

/**
 * Simplified service interface for AI image generation operations using OpenAI.
 * This interface focuses solely on OpenAI API interactions without storage concerns.
 */
interface OpenAIImageGenerationService {
    /**
     * Generates images using OpenAI API from processed image bytes and prompt.
     * Returns the raw image bytes without any storage operations.
     */
    fun generateImages(
        imageBytes: ByteArray,
        request: ImageGenerationRequest,
    ): List<ByteArray>

    /**
     * Generates images using OpenAI API from processed image bytes and prompt ID.
     * Returns the raw image bytes without any storage operations.
     */
    fun generateImages(
        imageBytes: ByteArray,
        promptId: Long,
    ): List<ByteArray>
}
