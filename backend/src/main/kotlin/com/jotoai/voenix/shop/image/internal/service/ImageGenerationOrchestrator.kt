package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/**
 * Orchestrator service for image generation that coordinates between generation services
 * without handling storage directly. This service determines which generation service to use
 * and delegates the actual work while maintaining proper separation of concerns.
 */
@Service
@Transactional(readOnly = true)
class ImageGenerationOrchestrator(
    private val publicImageGenerationService: PublicImageGenerationService,
    private val userImageGenerationService: UserImageGenerationService,
) {
    private val logger = LoggerFactory.getLogger(ImageGenerationOrchestrator::class.java)

    /**
     * Generates images for public (anonymous) users.
     */
    @Transactional
    fun generatePublicImage(
        imageFile: MultipartFile,
        request: PublicImageGenerationRequest,
    ): PublicImageGenerationResponse {
        logger.info("Orchestrating public image generation for prompt ID: ${request.promptId}")
        return publicImageGenerationService.generateImage(imageFile, request)
    }

    /**
     * Generates images for authenticated users.
     */
    @Transactional
    fun generateUserImage(
        imageFile: MultipartFile,
        request: PublicImageGenerationRequest,
        userId: Long,
    ): PublicImageGenerationResponse {
        logger.info("Orchestrating user image generation for user $userId with prompt ID: ${request.promptId}")
        return userImageGenerationService.generateImage(imageFile, request, userId)
    }

    /**
     * Generates images for authenticated users using a previously uploaded image.
     */
    @Transactional
    fun generateUserImageFromUpload(
        promptId: Long,
        userId: Long,
    ): PublicImageGenerationResponse {
        logger.info("Orchestrating user image generation from upload for user $userId with prompt ID: $promptId")

        // This is a simplified approach - in practice, you might want to retrieve the uploaded image
        // and create a MultipartFile wrapper, or extend the generation services to handle UUID-based generation
        throw UnsupportedOperationException(
            "Generation from uploaded image UUID requires additional implementation. " +
                "Consider extending UserImageGenerationService to handle UUID-based generation directly.",
        )
    }

    /**
     * Checks if a user or IP address is rate limited for image generation.
     */
    fun isRateLimited(
        userId: Long? = null,
        ipAddress: String? = null,
    ): Boolean =
        when {
            userId != null -> {
                // Check user-specific rate limits
                // This would delegate to a rate limiting service
                false // TODO: Implement user rate limiting check
            }
            ipAddress != null -> {
                // Check IP-based rate limits
                // This would delegate to a rate limiting service
                false // TODO: Implement IP rate limiting check
            }
            else -> {
                logger.warn("Rate limit check called without user ID or IP address")
                true // Default to rate limited if no identifier provided
            }
        }

    /**
     * Gets the current rate limit status for a user or IP.
     */
    fun getRateLimitStatus(
        userId: Long? = null,
        ipAddress: String? = null,
    ): RateLimitStatus {
        // This would delegate to a rate limiting service to get detailed status
        return RateLimitStatus(
            isLimited = isRateLimited(userId, ipAddress),
            remainingRequests = if (userId != null) 50 else 10, // Mock values
            resetTime = System.currentTimeMillis() + (60 * 60 * 1000), // 1 hour from now
        )
    }
}

/**
 * Data class representing rate limit status.
 */
data class RateLimitStatus(
    val isLimited: Boolean,
    val remainingRequests: Int,
    val resetTime: Long,
)
