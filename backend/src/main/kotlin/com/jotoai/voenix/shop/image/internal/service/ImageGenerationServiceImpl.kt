package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.common.exception.BadRequestException
import com.jotoai.voenix.shop.image.api.ImageGenerationService
import com.jotoai.voenix.shop.image.api.ImageStorageService
import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.image.internal.entity.GeneratedImage
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.openai.api.OpenAIImageFacade
import com.jotoai.voenix.shop.openai.api.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.user.api.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException
import java.time.LocalDateTime
import java.util.*
import org.springframework.core.io.Resource
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

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
    private val imageValidationService: ImageValidationService,
) : ImageGenerationService {
    companion object {
        private val logger = KotlinLogging.logger {}

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

        imageValidationService.validateImageFile(imageFile)
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
        cropArea: CropArea?,
    ): String {
        logger.info { "Processing user image generation request for user $userId with prompt ID: $promptId" }

        requireNotNull(uploadedImageUuid) { "Uploaded image UUID is required for user image generation" }

        checkUserRateLimit(userId)
        validatePrompt(promptId)

        // Validate user exists
        userService.getUserById(userId)

        return executeWithErrorHandling(
            operation = { processUserImageGeneration(promptId, uploadedImageUuid, userId, cropArea) },
            contextMessage = "generating image for user $userId",
        )
    }

    @Transactional
    override fun generateUserImageWithIds(
        promptId: Long,
        uploadedImageUuid: UUID?,
        userId: Long,
        cropArea: CropArea?,
    ): PublicImageGenerationResponse {
        logger.info { "Processing user image generation with IDs request for user $userId with prompt ID: $promptId" }

        requireNotNull(uploadedImageUuid) { "Uploaded image UUID is required for user image generation" }

        checkUserRateLimit(userId)
        validatePrompt(promptId)

        // Validate user exists
        userService.getUserById(userId)

        return executeWithErrorHandling(
            operation = { processUserImageGenerationWithIds(promptId, uploadedImageUuid, userId, cropArea) },
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

        // Store the image (cropped if needed) and load it for OpenAI processing
        val storedFilename = if (request.cropArea != null) {
            logger.info { "Storing cropped image with area: ${request.cropArea}" }
            imageStorageService.storeFile(imageFile, ImageType.PUBLIC, request.cropArea)
        } else {
            imageStorageService.storeFile(imageFile, ImageType.PUBLIC)
        }

        // Load the stored image file for OpenAI processing
        val storedImageResource = imageStorageService.loadFileAsResource(storedFilename, ImageType.PUBLIC)
        val processedImageFile =
            ResourceMultipartFile(
                resource = storedImageResource,
                filename = imageFile.originalFilename ?: "image.png",
                contentType = imageFile.contentType ?: "image/png",
            )

        // Generate images using OpenAI service
        val imageBytes = generateImagesWithOpenAI(processedImageFile, request.promptId)

        // Store each generated image and create database records
        val generatedImages =
            imageBytes.mapIndexed { index, bytes ->
                // Generate filename for this image
                val filename = "${UUID.randomUUID()}_generated_${index + 1}.png"

                // Store the image bytes
                imageStorageService.storeFile(bytes, filename, ImageType.PUBLIC)

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
        cropArea: CropArea?,
    ): String {
        logger.info { "Processing user image generation for user $userId with uploaded image UUID: $uploadedImageUuid" }

        // Retrieve the uploaded image entity
        val uploadedImage = imageStorageServiceImpl.getUploadedImageByUuid(uploadedImageUuid, userId)

        // Load the original image as MultipartFile
        val originalImageResource = imageStorageService.loadFileAsResource(uploadedImage.storedFilename, ImageType.PRIVATE)
        val originalMultipartFile =
            ResourceMultipartFile(
                resource = originalImageResource,
                filename = uploadedImage.originalFilename,
                contentType = "image/png",
            )

        // Store the image (cropped if needed) and load it for OpenAI processing
        val processedFilename = if (cropArea != null) {
            logger.info { "Storing cropped image with area: $cropArea for user $userId" }
            imageStorageService.storeFile(originalMultipartFile, ImageType.PRIVATE, cropArea)
        } else {
            // Use the existing uploaded image
            uploadedImage.storedFilename
        }

        // Load the processed image file for OpenAI processing
        val processedImageResource = imageStorageService.loadFileAsResource(processedFilename, ImageType.PRIVATE)
        val multipartFile =
            ResourceMultipartFile(
                resource = processedImageResource,
                filename = uploadedImage.originalFilename,
                contentType = "image/png",
            )

        // Generate images using OpenAI service
        val generatedImageBytes = generateImagesWithOpenAI(multipartFile, promptId)

        // Store the generated image(s) in user storage
        val generatedImages =
            generatedImageBytes.mapIndexed { index, generatedBytes ->
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
        cropArea: CropArea?,
    ): PublicImageGenerationResponse {
        logger.info {
            "Processing user image generation with IDs for user $userId " +
                "with uploaded image UUID: $uploadedImageUuid"
        }

        // Retrieve the uploaded image entity
        val uploadedImage = imageStorageServiceImpl.getUploadedImageByUuid(uploadedImageUuid, userId)

        // Load the original image as MultipartFile
        val originalImageResource = imageStorageService.loadFileAsResource(uploadedImage.storedFilename, ImageType.PRIVATE)
        val originalMultipartFile =
            ResourceMultipartFile(
                resource = originalImageResource,
                filename = uploadedImage.originalFilename,
                contentType = "image/png",
            )

        // Store the image (cropped if needed) and load it for OpenAI processing
        val processedFilename = if (cropArea != null) {
            logger.info { "Storing cropped image with area: $cropArea for user $userId (with IDs)" }
            imageStorageService.storeFile(originalMultipartFile, ImageType.PRIVATE, cropArea)
        } else {
            // Use the existing uploaded image
            uploadedImage.storedFilename
        }

        // Load the processed image file for OpenAI processing
        val processedImageResource = imageStorageService.loadFileAsResource(processedFilename, ImageType.PRIVATE)
        val multipartFile =
            ResourceMultipartFile(
                resource = processedImageResource,
                filename = uploadedImage.originalFilename,
                contentType = "image/png",
            )

        // Generate images using OpenAI service
        val generatedImageBytes = generateImagesWithOpenAI(multipartFile, promptId)

        // Store each generated image and create database records
        val generatedImages =
            generatedImageBytes.mapIndexed { index, generatedBytes ->
                logger.info { "Processing generated image ${index + 1} of ${generatedImageBytes.size}" }
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
            (1..4).map { index ->
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


    private fun createOpenAIRequest(request: PublicImageGenerationRequest): CreateImageEditRequest =
        CreateImageEditRequest(
            promptId = request.promptId,
            background = request.background,
            quality = request.quality,
            size = request.size,
            n = request.n,
        )

    private fun createOpenAIRequest(promptId: Long): CreateImageEditRequest =
        createOpenAIRequest(PublicImageGenerationRequest(promptId = promptId, n = 4))

    /**
     * Common helper method for generating images using OpenAI.
     * Handles the OpenAI API call and returns the generated image bytes.
     */
    private fun generateImagesWithOpenAI(
        processedImageFile: MultipartFile,
        promptId: Long,
    ): List<ByteArray> {
        val openAIRequest = createOpenAIRequest(promptId)
        val imageEditResponse = openAIImageFacade.editImageBytes(processedImageFile, openAIRequest)
        return imageEditResponse.imageBytes
    }

    private fun validatePrompt(promptId: Long) {
        val prompt = promptQueryService.getPromptById(promptId)
        if (!prompt.active) {
            throw BadRequestException("The selected prompt is not available")
        }
    }

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

/**
 * MultipartFile wrapper for Spring Resource objects.
 */
private class ResourceMultipartFile(
    private val resource: Resource,
    private val filename: String,
    private val contentType: String,
) : MultipartFile {
    override fun getName(): String = "file"

    override fun getOriginalFilename(): String = filename

    override fun getContentType(): String = contentType

    override fun isEmpty(): Boolean = !resource.exists() || resource.contentLength() == 0L

    override fun getSize(): Long = resource.contentLength()

    override fun getBytes(): ByteArray = resource.inputStream.use { it.readAllBytes() }

    override fun getInputStream(): java.io.InputStream = resource.inputStream

    override fun transferTo(dest: java.io.File) {
        resource.inputStream.use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
