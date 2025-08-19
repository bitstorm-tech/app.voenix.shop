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
import com.jotoai.voenix.shop.user.api.UserService
import jakarta.servlet.http.HttpServletRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.time.LocalDateTime
import java.util.*

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
    private val userService: UserService,
    private val imageStorageService: ImageStorageService,
    private val storagePathService: StoragePathService,
    private val imageStorageServiceImpl: ImageStorageServiceImpl,
    private val request: HttpServletRequest,
) : ImageGenerationService {
    companion object {
        private val logger = KotlinLogging.logger {}

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
        logger.info { "Processing public image generation request for prompt ID: ${request.promptId}" }

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
        logger.info { "Processing user image generation request for user $userId with prompt ID: $promptId" }

        requireNotNull(uploadedImageUuid) { "Uploaded image UUID is required for user image generation" }

        checkUserRateLimit(userId)
        validatePrompt(promptId)

        // Validate user exists
        userService.getUserById(userId)

        return executeWithErrorHandling(
            operation = { processUserImageGeneration(promptId, uploadedImageUuid, userId) },
            contextMessage = "generating image for user $userId",
        )
    }

    @Transactional
    override fun generateUserImageWithIds(
        promptId: Long,
        uploadedImageUuid: UUID?,
        userId: Long,
    ): PublicImageGenerationResponse {
        logger.info { "Processing user image generation with IDs request for user $userId with prompt ID: $promptId" }

        requireNotNull(uploadedImageUuid) { "Uploaded image UUID is required for user image generation" }

        checkUserRateLimit(userId)
        validatePrompt(promptId)

        // Validate user exists
        userService.getUserById(userId)

        return executeWithErrorHandling(
            operation = { processUserImageGenerationWithIds(promptId, uploadedImageUuid, userId) },
            contextMessage = "generating image with IDs for user $userId",
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
                logger.warn { "Rate limit check called without user ID or IP address" }
                true // Default to rate limited if no identifier provided
            }
        }

    private fun processPublicImageGeneration(
        imageFile: MultipartFile,
        request: PublicImageGenerationRequest,
        ipAddress: String,
    ): PublicImageGenerationResponse {
        val openAIRequest = createOpenAIRequest(request)
        logger.debug { "Generated OpenAI request: $openAIRequest" }

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

        logger.info { "Successfully generated ${imageUrls.size} images for public user with IP: $ipAddress" }

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
        logger.info { "Processing user image generation for user $userId with uploaded image UUID: $uploadedImageUuid" }

        // Retrieve the uploaded image entity
        val uploadedImage = imageStorageServiceImpl.getUploadedImageByUuid(uploadedImageUuid, userId)

        // Load the image data from storage
        val (imageBytes, contentType) = imageStorageServiceImpl.getUserImageData(uploadedImage.storedFilename, userId)

        // Create a MultipartFile wrapper for the stored image
        val multipartFile =
            ByteArrayMultipartFile(
                bytes = imageBytes,
                filename = uploadedImage.originalFilename,
                contentType = contentType,
                fieldName = "image",
            )

        // Create OpenAI request to generate 4 images (matching the public generation)
        val request = PublicImageGenerationRequest(promptId = promptId, n = 4)
        val openAIRequest = createOpenAIRequest(request)

        logger.debug { "Generated OpenAI request for user image: $openAIRequest" }

        // Generate image using OpenAI service
        val imageEditResponse = openAIImageFacade.editImageBytes(multipartFile, openAIRequest)

        // Store the generated image(s) in user storage
        val generatedImages =
            imageEditResponse.imageBytes.mapIndexed { index, generatedBytes ->
                imageStorageServiceImpl.storeGeneratedImage(
                    imageBytes = generatedBytes,
                    uploadedImage = uploadedImage,
                    promptId = promptId,
                    generationNumber = index + 1,
                )
            }

        // Return the filename of the first generated image
        val firstGeneratedImage =
            generatedImages.firstOrNull()
                ?: throw IllegalStateException("No images were generated")

        logger.info { "Successfully generated ${generatedImages.size} image(s) for user $userId" }

        return firstGeneratedImage.filename
    }

    private fun processUserImageGenerationWithIds(
        promptId: Long,
        uploadedImageUuid: UUID,
        userId: Long,
    ): PublicImageGenerationResponse {
        logger.info {
            "Processing user image generation with IDs for user $userId " +
                "with uploaded image UUID: $uploadedImageUuid"
        }

        // Retrieve the uploaded image entity
        val uploadedImage = imageStorageServiceImpl.getUploadedImageByUuid(uploadedImageUuid, userId)

        // Load the image data from storage
        val (imageBytes, contentType) = imageStorageServiceImpl.getUserImageData(uploadedImage.storedFilename, userId)

        // Create a MultipartFile wrapper for the stored image
        val multipartFile =
            ByteArrayMultipartFile(
                bytes = imageBytes,
                filename = uploadedImage.originalFilename,
                contentType = contentType,
                fieldName = "image",
            )

        // Create OpenAI request to generate 4 images (matching the public generation)
        val request = PublicImageGenerationRequest(promptId = promptId, n = 4)
        val openAIRequest = createOpenAIRequest(request)

        logger.debug { "Generated OpenAI request for user image with IDs: $openAIRequest" }

        // Generate image using OpenAI service
        val imageEditResponse = openAIImageFacade.editImageBytes(multipartFile, openAIRequest)

        // Store each generated image and create database records
        val generatedImages =
            imageEditResponse.imageBytes.mapIndexed { index, generatedBytes ->
                logger.info { "Processing generated image ${index + 1} of ${imageEditResponse.imageBytes.size}" }
                val generatedImage =
                    imageStorageServiceImpl.storeGeneratedImage(
                        imageBytes = generatedBytes,
                        uploadedImage = uploadedImage,
                        promptId = promptId,
                        generationNumber = index + 1,
                    )
                logger.info {
                    "Saved generated image to database with ID: ${generatedImage.id}, " +
                        "filename: ${generatedImage.filename}"
                }
                generatedImage
            }

        // Create image URLs for user images
        val baseUuid = uploadedImage.uuid.toString()
        val imageUrls =
            (1..request.n).map { index ->
                "/api/user/images/${baseUuid}_generated_$index.png"
            }

        // Extract the generated image IDs
        val generatedImageIds = generatedImages.mapNotNull { it.id }

        logger.info { "Generated images count: ${generatedImages.size}" }
        generatedImages.forEachIndexed { index, img ->
            logger.info { "Image $index: ID=${img.id}, filename=${img.filename}" }
        }
        logger.info {
            "Successfully generated ${generatedImages.size} images with IDs $generatedImageIds " +
                "for user $userId"
        }

        return PublicImageGenerationResponse(
            imageUrls = imageUrls,
            generatedImageIds = generatedImageIds,
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
        logger.debug { "$identifier has generated $generationCount images in the last $rateLimitHours $timeUnit" }
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
            logger.error(e) { "Database error $contextMessage" }
            throw RuntimeException("Failed to generate image. Please try again later.")
        } catch (e: IOException) {
            logger.error(e) { "I/O error $contextMessage" }
            throw RuntimeException("Failed to generate image. Please try again later.")
        } catch (e: IllegalStateException) {
            logger.error(e) { "State error $contextMessage" }
            throw RuntimeException("Failed to generate image. Please try again later.")
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Argument error $contextMessage" }
            throw RuntimeException("Failed to generate image. Please try again later.")
        }
}
