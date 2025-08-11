package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.common.exception.BadRequestException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.openai.api.OpenAIImageFacade
import com.jotoai.voenix.shop.openai.api.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.time.LocalDateTime

/**
 * Abstract base class for image generation services that provides common functionality
 * like file validation, rate limiting patterns, and error handling.
 */
abstract class BaseImageGenerationService(
    protected val openAIImageFacade: OpenAIImageFacade,
    protected val promptQueryService: PromptQueryService,
    protected val generatedImageRepository: GeneratedImageRepository,
) {
    companion object {
        @JvmStatic
        protected val logger = LoggerFactory.getLogger(BaseImageGenerationService::class.java)

        // Common file validation constants
        protected const val MAX_FILE_SIZE = 10 * 1024 * 1024

        // 10MB
        @JvmStatic
        protected val ALLOWED_CONTENT_TYPES = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")
    }

    /**
     * Validates the uploaded image file for size and content type.
     * @param file The multipart file to validate
     * @throws BadRequestException if validation fails
     */
    protected fun validateImageFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw BadRequestException("Image file is required")
        }

        if (file.size > MAX_FILE_SIZE) {
            throw BadRequestException("Image file size must be less than 10MB")
        }

        val contentType = file.contentType?.lowercase() ?: ""
        if (contentType !in ALLOWED_CONTENT_TYPES) {
            throw BadRequestException("Invalid image format. Allowed formats: JPEG, PNG, WebP")
        }
    }

    /**
     * Validates that the prompt is active and available for use.
     * @param promptId The ID of the prompt to validate
     * @throws BadRequestException if the prompt is inactive
     */
    protected fun validatePrompt(promptId: Long) {
        val prompt = promptQueryService.getPromptById(promptId)
        if (!prompt.active) {
            throw BadRequestException("The selected prompt is not available")
        }
    }

    /**
     * Creates an OpenAI request from the public image generation request.
     */
    protected fun createOpenAIRequest(request: PublicImageGenerationRequest): CreateImageEditRequest =
        CreateImageEditRequest(
            promptId = request.promptId,
            background = request.background,
            quality = request.quality,
            size = request.size,
            n = request.n,
        )

    /**
     * Template method for rate limiting. Implementations should provide specific rate limiting logic.
     * @param identifier The identifier to check rate limits for (user ID, IP address, etc.)
     */
    protected abstract fun checkRateLimit(identifier: String)

    /**
     * Template method for processing the image generation after validation and rate limiting.
     * @param imageFile The uploaded image file
     * @param request The image generation request
     * @param identifier The identifier for tracking (user ID, IP address, etc.)
     */
    protected abstract fun processImageGeneration(
        imageFile: MultipartFile,
        request: PublicImageGenerationRequest,
        identifier: String,
    ): PublicImageGenerationResponse

    /**
     * Common error handling wrapper for image generation operations.
     * @param operation The operation to execute
     * @param contextMessage Context message for logging errors
     */
    protected fun <T> executeWithErrorHandling(
        operation: () -> T,
        contextMessage: String,
    ): T =
        try {
            operation()
        } catch (e: BadRequestException) {
            throw e
        } catch (e: ResourceNotFoundException) {
            throw e
        } catch (e: DataAccessException) {
            logger.error("Database error $contextMessage", e)
            throw RuntimeException("Failed to generate image. Please try again later.")
        } catch (e: IOException) {
            logger.error("I/O error $contextMessage", e)
            throw RuntimeException("Failed to generate image. Please try again later.")
        } catch (e: IllegalStateException) {
            logger.error("State error $contextMessage", e)
            throw RuntimeException("Failed to generate image. Please try again later.")
        } catch (e: IllegalArgumentException) {
            logger.error("Argument error $contextMessage", e)
            throw RuntimeException("Failed to generate image. Please try again later.")
        }

    /**
     * Helper method for checking rate limits with time-based counting.
     * @param identifier The identifier to check
     * @param rateLimitHours The number of hours to look back
     * @param maxGenerations The maximum number of generations allowed
     * @param countFunction Function to count existing generations
     * @param rateLimitMessage Custom rate limit message
     */
    protected fun checkTimeBasedRateLimit(
        identifier: String,
        rateLimitHours: Int,
        maxGenerations: Int,
        countFunction: (String, LocalDateTime) -> Long,
        rateLimitMessage: String,
    ) {
        val startTime = LocalDateTime.now().minusHours(rateLimitHours.toLong())
        val generationCount = countFunction(identifier, startTime)

        if (generationCount >= maxGenerations) {
            throw BadRequestException(rateLimitMessage)
        }

        val timeUnit = if (rateLimitHours == 1) "hour" else "hours"
        logger.debug("$identifier has generated $generationCount images in the last $rateLimitHours $timeUnit")
    }
}
