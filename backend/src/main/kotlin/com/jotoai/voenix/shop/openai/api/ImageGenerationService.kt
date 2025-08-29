package com.jotoai.voenix.shop.openai.api

import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.openai.api.dto.ImageGenerationRequest
import com.jotoai.voenix.shop.openai.api.dto.ImageGenerationResponse
import org.springframework.web.multipart.MultipartFile
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
        request: ImageGenerationRequest,
        ipAddress: String,
        imageFile: MultipartFile,
    ): ImageGenerationResponse

    /**
     * Generates an AI image for authenticated users.
     */
    fun generateUserImage(
        promptId: Long,
        uploadedImageUuid: UUID?,
        userId: Long,
        cropArea: CropArea? = null,
    ): String

    /**
     * Generates AI images for authenticated users and returns complete response with IDs.
     * This method is similar to generateUserImage but returns the full response including
     * generated image IDs for tracking purposes.
     */
    fun generateUserImageWithIds(
        promptId: Long,
        uploadedImageUuid: UUID?,
        userId: Long,
        cropArea: CropArea? = null,
    ): ImageGenerationResponse

    /**
     * Checks rate limiting for image generation.
     */
    fun isRateLimited(
        userId: Long?,
        ipAddress: String?,
    ): Boolean
}
