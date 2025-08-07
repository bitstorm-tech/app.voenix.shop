package com.jotoai.voenix.shop.image.api

import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import java.util.UUID

/**
 * Service for AI image generation operations.
 * This interface defines operations for generating AI images from prompts and uploaded images.
 */
interface ImageGenerationService {
    /**
     * Generates an AI image from a prompt and optional uploaded image for public users.
     */
    fun generatePublicImage(
        request: PublicImageGenerationRequest,
        ipAddress: String,
    ): PublicImageGenerationResponse

    /**
     * Generates an AI image for authenticated users.
     */
    fun generateUserImage(
        promptId: Long,
        uploadedImageUuid: UUID?,
        userId: Long,
    ): String

    /**
     * Checks rate limiting for image generation.
     */
    fun isRateLimited(
        userId: Long?,
        ipAddress: String?,
    ): Boolean
}
