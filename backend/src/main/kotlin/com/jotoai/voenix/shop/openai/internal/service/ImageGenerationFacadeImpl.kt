package com.jotoai.voenix.shop.openai.internal.service

import com.jotoai.voenix.shop.application.api.exception.BadRequestException
import com.jotoai.voenix.shop.application.api.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.CountFilter
import com.jotoai.voenix.shop.image.api.ImageData
import com.jotoai.voenix.shop.image.api.ImageMetadata
import com.jotoai.voenix.shop.image.api.ImageService
import com.jotoai.voenix.shop.image.api.ValidationRequest
import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.UploadedImageDto
import com.jotoai.voenix.shop.image.api.exceptions.ImageException
import com.jotoai.voenix.shop.openai.api.ImageGenerationService
import com.jotoai.voenix.shop.openai.api.OpenAIImageGenerationService
import com.jotoai.voenix.shop.openai.api.dto.ImageGenerationRequest
import com.jotoai.voenix.shop.openai.api.dto.ImageGenerationResponse
import com.jotoai.voenix.shop.openai.api.exception.ImageGenerationException
import com.jotoai.voenix.shop.user.api.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.UUID

/**
 * Implementation of ImageGenerationService that orchestrates AI image generation.
 * This service handles the complete workflow from rate limiting to AI generation to storage.
 */
@Service
class ImageGenerationFacadeImpl(
    private val imageService: ImageService,
    private val openAIImageGenerationService: OpenAIImageGenerationService,
    private val userService: UserService,
) : ImageGenerationService {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val PUBLIC_RATE_LIMIT_HOURS = 1
        private const val PUBLIC_MAX_GENERATIONS_PER_HOUR = 10
        private const val USER_RATE_LIMIT_HOURS = 24
        private const val USER_MAX_GENERATIONS_PER_DAY = 50
    }

    @Transactional
    override fun generatePublicImage(
        request: ImageGenerationRequest,
        ipAddress: String,
        imageFile: MultipartFile,
    ): ImageGenerationResponse {
        logger.info { "Generating image: public, prompt=${request.promptId}" }

        val validation = imageService.validate(ValidationRequest.FileUpload(imageFile))
        if (!validation.valid) {
            throw ImageException.Processing(validation.message ?: "Image validation failed")
        }

        val hourAgo = LocalDateTime.now().minusHours(PUBLIC_RATE_LIMIT_HOURS.toLong())
        val count = imageService.count(CountFilter(ipAddress = ipAddress, after = hourAgo))
        checkRateLimit(
            count,
            PUBLIC_MAX_GENERATIONS_PER_HOUR,
            "Rate limit exceeded. Max $PUBLIC_MAX_GENERATIONS_PER_HOUR images per hour.",
        )

        try {
            return processPublicImageGeneration(
                imageFile,
                request.promptId,
                ipAddress,
                request.cropArea,
            )
        } catch (e: ImageException.Storage) {
            handleSystemError(e, "public image generation")
        }
    }

    override fun generateUserImage(
        promptId: Long,
        uploadedImageUuid: UUID?,
        userId: Long,
        cropArea: CropArea?,
    ): String {
        logger.info { "Generating user image: user=$userId, prompt=$promptId" }
        requireNotNull(uploadedImageUuid) { "uploadedImageUuid is required for user image generation" }
        val response = generateUserImageWithIds(promptId, uploadedImageUuid, userId, cropArea)
        return response.imageUrls.firstOrNull() ?: throw ImageGenerationException("No images generated")
    }

    @Transactional
    override fun generateUserImageWithIds(
        promptId: Long,
        uploadedImageUuid: UUID?,
        userId: Long,
        cropArea: CropArea?,
    ): ImageGenerationResponse {
        logger.info { "Generating image: user=$userId, prompt=$promptId" }
        requireNotNull(uploadedImageUuid) { "uploadedImageUuid is required for user image generation" }

        userService.getUserById(userId)

        val dayAgo = LocalDateTime.now().minusHours(USER_RATE_LIMIT_HOURS.toLong())
        val count = imageService.count(CountFilter(userId = userId, after = dayAgo))
        checkRateLimit(
            count,
            USER_MAX_GENERATIONS_PER_DAY,
            "Rate limit exceeded. Max $USER_MAX_GENERATIONS_PER_DAY images per day.",
        )

        try {
            return processImageGeneration(
                uploadedImageUuid,
                promptId,
                userId,
                null,
                cropArea,
            )
        } catch (e: ImageException.Storage) {
            handleSystemError(e, "user image generation")
        }
    }

    override fun isRateLimited(
        userId: Long?,
        ipAddress: String?,
    ): Boolean {
        logger.debug { "Rate limit check for userId=$userId, ipAddress=$ipAddress" }

        return try {
            if (userId != null) {
                val dayAgo = LocalDateTime.now().minusHours(USER_RATE_LIMIT_HOURS.toLong())
                val count = imageService.count(CountFilter(userId = userId, after = dayAgo))
                count >= USER_MAX_GENERATIONS_PER_DAY
            } else if (ipAddress != null) {
                val hourAgo = LocalDateTime.now().minusHours(PUBLIC_RATE_LIMIT_HOURS.toLong())
                val count = imageService.count(CountFilter(ipAddress = ipAddress, after = hourAgo))
                count >= PUBLIC_MAX_GENERATIONS_PER_HOUR
            } else {
                false
            }
        } catch (e: IllegalArgumentException) {
            logger.warn(e) { "Error checking rate limit" }
            false // Default to allowing if we can't check
        } catch (e: IllegalStateException) {
            logger.warn(e) { "Error checking rate limit" }
            false // Default to allowing if we can't check
        }
    }

    private fun checkRateLimit(
        count: Long,
        limit: Int,
        errorMessage: String,
    ) {
        if (count >= limit) {
            throw BadRequestException(errorMessage)
        }
    }

    @Suppress("UnusedParameter")
    private fun processImageGeneration(
        uploadedImageUuid: UUID,
        promptId: Long,
        userId: Long,
        ipAddress: String?,
        cropArea: CropArea?,
    ): ImageGenerationResponse {
        val uploadedImage = imageService.getUploadedImageByUuid(uploadedImageUuid, userId) as UploadedImageDto

        val imageContent = imageService.get(uploadedImage.filename, userId)
        val imageBytes = imageContent.bytes

        val generatedBytes = openAIImageGenerationService.generateImages(imageBytes, promptId)

        // Get the uploaded image entity ID for storage
        val uploadedImageId =
            uploadedImage.id
                ?: throw ResourceNotFoundException("Uploaded image ID not found")

        val generatedImages =
            generatedBytes.mapIndexed { index, bytes ->
                val imageData = ImageData.Bytes(bytes, "generated_${index + 1}.png")
                val metadata =
                    ImageMetadata(
                        type = ImageType.GENERATED,
                        userId = userId,
                        promptId = promptId,
                        uploadedImageId = uploadedImageId,
                        generationNumber = index + 1,
                        ipAddress = ipAddress,
                    )
                imageService.store(imageData, metadata) as GeneratedImageDto
            }

        val imageUrls =
            generatedImages.map { dto ->
                imageService.getUrl(dto.filename, ImageType.PRIVATE)
            }

        val imageIds = generatedImages.mapNotNull { it.id }
        logger.info { "Generated ${imageIds.size} images" }

        return ImageGenerationResponse(
            imageUrls = imageUrls,
            generatedImageIds = imageIds,
        )
    }

    @Suppress("UnusedParameter")
    private fun processPublicImageGeneration(
        imageFile: MultipartFile,
        promptId: Long,
        ipAddress: String,
        cropArea: CropArea?,
    ): ImageGenerationResponse {
        val originalBytes = imageFile.bytes
        val imageBytes = originalBytes

        val generatedBytes = openAIImageGenerationService.generateImages(imageBytes, promptId)

        val generatedImages =
            generatedBytes.mapIndexed { index, bytes ->
                val imageData = ImageData.Bytes(bytes, "public_generated_${index + 1}.png")
                val metadata =
                    ImageMetadata(
                        type = ImageType.PUBLIC,
                        promptId = promptId,
                        ipAddress = ipAddress,
                        generationNumber = index + 1,
                    )
                imageService.store(imageData, metadata) as GeneratedImageDto
            }

        val imageUrls =
            generatedImages.map { generatedImageDto ->
                imageService.getUrl(generatedImageDto.filename, ImageType.PUBLIC)
            }

        val imageIds = generatedImages.mapNotNull { it.id }
        logger.info { "Generated ${imageIds.size} images" }

        return ImageGenerationResponse(
            imageUrls = imageUrls,
            generatedImageIds = imageIds,
        )
    }

    private fun handleSystemError(
        e: Exception,
        operation: String,
    ): Nothing {
        logger.error(e) { "Error during $operation" }
        throw ImageException.Processing("Failed to generate image. Please try again later.", e)
    }
}
