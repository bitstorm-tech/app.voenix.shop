package com.jotoai.voenix.shop.openai.api

import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import org.springframework.web.multipart.MultipartFile

/**
 * Simplified service interface for AI image generation operations using OpenAI.
 * This interface focuses solely on OpenAI API interactions without storage concerns.
 */
interface OpenAIImageGenerationService {
    /**
     * Generates images using OpenAI API from a processed image file and prompt.
     * Returns the raw image bytes without any storage operations.
     */
    fun generateImages(
        processedImageFile: MultipartFile,
        request: PublicImageGenerationRequest,
    ): List<ByteArray>

    /**
     * Generates images using OpenAI API from a processed image file and prompt ID.
     * Returns the raw image bytes without any storage operations.
     */
    fun generateImages(
        processedImageFile: MultipartFile,
        promptId: Long,
    ): List<ByteArray>
}
