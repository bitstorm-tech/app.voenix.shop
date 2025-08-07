package com.jotoai.voenix.shop.domain.images.service

import com.jotoai.voenix.shop.common.exception.BadRequestException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.images.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.domain.images.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.domain.images.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.domain.openai.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.domain.openai.service.OpenAIImageService
import com.jotoai.voenix.shop.domain.prompts.service.PromptService
import com.jotoai.voenix.shop.user.api.UserQueryService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class UserImageGenerationService(
    private val openAIImageService: OpenAIImageService,
    private val promptService: PromptService,
    private val generatedImageRepository: GeneratedImageRepository,
    private val userQueryService: UserQueryService,
    private val storagePathService: StoragePathService,
    private val userImageStorageService: UserImageStorageService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserImageGenerationService::class.java)
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB
        private val ALLOWED_CONTENT_TYPES = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")
        private const val RATE_LIMIT_HOURS = 24
        private const val MAX_GENERATIONS_PER_DAY = 50
    }

    @Transactional
    fun generateImage(
        imageFile: MultipartFile,
        request: PublicImageGenerationRequest,
        userId: Long,
    ): PublicImageGenerationResponse {
        validateImageFile(imageFile)
        checkUserRateLimit(userId)

        val prompt = promptService.getPromptById(request.promptId)
        if (!prompt.active) {
            throw BadRequestException("The selected prompt is not available")
        }

        logger.info("Processing authenticated image generation request for user $userId with prompt ID: ${request.promptId}")

        try {
            // Validate user exists
            userQueryService.getUserById(userId)

            // Store the uploaded image first using the new storage pattern
            val uploadedImage = userImageStorageService.storeUploadedImage(imageFile, userId)
            logger.info("Stored uploaded image with UUID: ${uploadedImage.uuid}")

            val openAIRequest =
                CreateImageEditRequest(
                    promptId = request.promptId,
                    background = request.background,
                    quality = request.quality,
                    size = request.size,
                    n = request.n,
                )

            logger.debug("Generated OpenAI request: {}", openAIRequest)

            // Generate images using OpenAI service (get raw bytes)
            val imageEditResponse = openAIImageService.editImageBytes(imageFile, openAIRequest)

            // Store each generated image using the new storage pattern
            val generatedImages =
                imageEditResponse.imageBytes.mapIndexed { index, imageBytes ->
                    val generationNumber = index + 1
                    userImageStorageService.storeGeneratedImage(
                        imageBytes = imageBytes,
                        uploadedImage = uploadedImage,
                        promptId = request.promptId,
                        generationNumber = generationNumber,
                    )
                }

            // Build URLs and IDs for the generated images
            val imageUrls =
                generatedImages.map { generatedImage ->
                    "/api/user/images/${generatedImage.filename}"
                }

            val imageIds =
                generatedImages.mapNotNull { generatedImage ->
                    generatedImage.id
                }

            logger.info("Successfully generated ${imageUrls.size} images for user $userId")

            return PublicImageGenerationResponse(
                imageUrls = imageUrls,
                generatedImageIds = imageIds,
            )
        } catch (e: Exception) {
            logger.error("Error generating image for user $userId", e)
            when (e) {
                is BadRequestException -> throw e
                is ResourceNotFoundException -> throw e
                else -> throw RuntimeException("Failed to generate image. Please try again later.")
            }
        }
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

    private fun checkUserRateLimit(userId: Long) {
        val startTime = LocalDateTime.now().minusHours(RATE_LIMIT_HOURS.toLong())
        val generationCount = generatedImageRepository.countByUserIdAndGeneratedAtAfter(userId, startTime)

        if (generationCount >= MAX_GENERATIONS_PER_DAY) {
            throw BadRequestException(
                "Rate limit exceeded. You can generate up to $MAX_GENERATIONS_PER_DAY images per day. Please try again later.",
            )
        }

        logger.debug("User $userId has generated $generationCount images in the last $RATE_LIMIT_HOURS hours")
    }
}
