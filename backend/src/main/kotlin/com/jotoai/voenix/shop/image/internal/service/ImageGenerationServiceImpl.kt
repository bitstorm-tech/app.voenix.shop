package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.common.exception.BadRequestException
import com.jotoai.voenix.shop.image.api.ImageGenerationService
import com.jotoai.voenix.shop.image.api.ImageStorageService
import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.image.internal.entity.GeneratedImage
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.openai.api.OpenAIImageFacade
import com.jotoai.voenix.shop.openai.api.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.user.api.UserQueryService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.time.LocalDateTime
import java.util.UUID

/**
 * Consolidated implementation of ImageGenerationService that handles both public and user image generation.
 * This service combines the functionality previously spread across multiple services for better maintainability.
 */
@Service
@Transactional(readOnly = true)
class ImageGenerationServiceImpl(
    private val openAIImageFacade: OpenAIImageFacade,
    private val promptQueryService: PromptQueryService,
    private val generatedImageRepository: GeneratedImageRepository,
    private val userQueryService: UserQueryService,
    private val imageStorageService: ImageStorageService,
    private val storagePathService: StoragePathService,
    private val request: HttpServletRequest,
) : ImageGenerationService {
    companion object {
        private val logger = LoggerFactory.getLogger(ImageGenerationServiceImpl::class.java)

        // File validation constants
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB
        private val ALLOWED_CONTENT_TYPES = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")

        // Rate limiting constants
        private const val PUBLIC_RATE_LIMIT_HOURS = 1
        private const val PUBLIC_MAX_GENERATIONS_PER_HOUR = 10
        private const val USER_RATE_LIMIT_HOURS = 24
        private const val USER_MAX_GENERATIONS_PER_DAY = 50
    }

    @Transactional
    override fun generatePublicImage(
        request: PublicImageGenerationRequest,
        ipAddress: String,
        imageFile: MultipartFile,
    ): PublicImageGenerationResponse {
        logger.info("Processing public image generation request for prompt ID: ${request.promptId}")

        validateImageFile(imageFile)
        checkPublicRateLimit(ipAddress)
        validatePrompt(request.promptId)

        return executeWithErrorHandling(
            operation = { processPublicImageGeneration(imageFile, request, ipAddress) },
            contextMessage = "generating image for public user with IP: $ipAddress",
        )
    }

    @Transactional
    override fun generateUserImage(
        promptId: Long,
        uploadedImageUuid: UUID?,
        userId: Long,
    ): String {
        logger.info("Processing user image generation request for user $userId with prompt ID: $promptId")

        requireNotNull(uploadedImageUuid) { "Uploaded image UUID is required for user image generation" }

        checkUserRateLimit(userId)
        validatePrompt(promptId)

        // Validate user exists
        userQueryService.getUserById(userId)

        return executeWithErrorHandling(
            operation = { processUserImageGeneration(promptId, uploadedImageUuid, userId) },
            contextMessage = "generating image for user $userId",
        )
    }

    override fun isRateLimited(
        userId: Long?,
        ipAddress: String?,
    ): Boolean =
        when {
            userId != null -> isUserRateLimited(userId)
            ipAddress != null -> isPublicRateLimited(ipAddress)
            else -> {
                logger.warn("Rate limit check called without user ID or IP address")
                true // Default to rate limited if no identifier provided
            }
        }

    private fun processPublicImageGeneration(
        imageFile: MultipartFile,
        request: PublicImageGenerationRequest,
        ipAddress: String,
    ): PublicImageGenerationResponse {
        val openAIRequest = createOpenAIRequest(request)
        logger.debug("Generated OpenAI request: {}", openAIRequest)

        // Generate images using OpenAI service
        val imageEditResponse = openAIImageFacade.editImageBytes(imageFile, openAIRequest)

        // Store each generated image and create database records
        val generatedImages =
            imageEditResponse.imageBytes.mapIndexed { index, imageBytes ->
                // Generate filename for this image
                val filename = "${UUID.randomUUID()}_generated_${index + 1}.png"

                // Store the image bytes
                imageStorageService.storeFile(imageBytes, filename, ImageType.PUBLIC)

                // Create and save the database record
                val generatedImage =
                    GeneratedImage(
                        filename = filename,
                        promptId = request.promptId,
                        userId = null, // Anonymous user
                        ipAddress = ipAddress,
                        generatedAt = LocalDateTime.now(),
                    )
                generatedImageRepository.save(generatedImage)
            }

        // Convert filenames to full URLs using StoragePathService
        val imageUrls =
            generatedImages.map { generatedImage ->
                storagePathService.getImageUrl(ImageType.PUBLIC, generatedImage.filename)
            }

        val imageIds = generatedImages.mapNotNull { it.id }

        logger.info("Successfully generated ${imageUrls.size} images for public user with IP: $ipAddress")

        return PublicImageGenerationResponse(
            imageUrls = imageUrls,
            generatedImageIds = imageIds,
        )
    }

    private fun processUserImageGeneration(
        promptId: Long,
        uploadedImageUuid: UUID,
        userId: Long,
    ): String {
        // This is a simplified implementation - in reality, we need to retrieve the uploaded image
        // and process it through OpenAI. For now, this matches the original implementation limitation.
        throw UnsupportedOperationException(
            "User image generation from UUID requires retrieving the uploaded image file. " +
                "This functionality needs to be implemented to convert UUID back to MultipartFile " +
                "or extend the OpenAI facade to work with stored images directly.",
        )
    }

    private fun checkPublicRateLimit(ipAddress: String) {
        checkTimeBasedRateLimit(
            identifier = "IP $ipAddress",
            rateLimitHours = PUBLIC_RATE_LIMIT_HOURS,
            maxGenerations = PUBLIC_MAX_GENERATIONS_PER_HOUR,
            countFunction = { _, startTime ->
                generatedImageRepository.countByIpAddressAndGeneratedAtAfter(ipAddress, startTime)
            },
            rateLimitMessage =
                "Rate limit exceeded. You can generate up to " +
                    "$PUBLIC_MAX_GENERATIONS_PER_HOUR images per hour. Please try again later.",
        )
    }

    private fun checkUserRateLimit(userId: Long) {
        checkTimeBasedRateLimit(
            identifier = "User $userId",
            rateLimitHours = USER_RATE_LIMIT_HOURS,
            maxGenerations = USER_MAX_GENERATIONS_PER_DAY,
            countFunction = { _, startTime ->
                generatedImageRepository.countByUserIdAndGeneratedAtAfter(userId, startTime)
            },
            rateLimitMessage =
                "Rate limit exceeded. You can generate up to " +
                    "$USER_MAX_GENERATIONS_PER_DAY images per day. Please try again later.",
        )
    }

    private fun isPublicRateLimited(ipAddress: String): Boolean =
        try {
            checkPublicRateLimit(ipAddress)
            false
        } catch (e: BadRequestException) {
            true
        }

    private fun isUserRateLimited(userId: Long): Boolean =
        try {
            checkUserRateLimit(userId)
            false
        } catch (e: BadRequestException) {
            true
        }

    private fun validateImageFile(file: MultipartFile) {
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

    private fun validatePrompt(promptId: Long) {
        val prompt = promptQueryService.getPromptById(promptId)
        if (!prompt.active) {
            throw BadRequestException("The selected prompt is not available")
        }
    }

    private fun createOpenAIRequest(request: PublicImageGenerationRequest): CreateImageEditRequest =
        CreateImageEditRequest(
            promptId = request.promptId,
            background = request.background,
            quality = request.quality,
            size = request.size,
            n = request.n,
        )

    private fun checkTimeBasedRateLimit(
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

    private fun <T> executeWithErrorHandling(
        operation: () -> T,
        contextMessage: String,
    ): T =
        try {
            operation()
        } catch (e: BadRequestException) {
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

    private fun getClientIpAddress(): String {
        // Check for forwarded IP addresses (when behind a proxy/load balancer)
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",")[0].trim()
        }

        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp
        }

        // Fall back to remote address
        return request.remoteAddr
    }
}
