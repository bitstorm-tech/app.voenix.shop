package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.openai.api.OpenAIImageFacade
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.user.api.UserQueryService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional(readOnly = true)
class UserImageGenerationService(
    openAIImageFacade: OpenAIImageFacade,
    promptQueryService: PromptQueryService,
    generatedImageRepository: GeneratedImageRepository,
    private val userQueryService: UserQueryService,
    private val userImageStorageService: UserImageStorageService,
) : BaseImageGenerationService(openAIImageFacade, promptQueryService, generatedImageRepository) {
    companion object {
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
        checkRateLimit(userId.toString())
        validatePrompt(request.promptId)

        logger.info(
            "Processing authenticated image generation request for user $userId " +
                    "with prompt ID: ${request.promptId}"
        )

        return executeWithErrorHandling(
            operation = { processImageGeneration(imageFile, request, userId.toString()) },
            contextMessage = "generating image for user $userId",
        )
    }

    override fun checkRateLimit(identifier: String) {
        val userId = identifier.toLong()
        checkTimeBasedRateLimit(
            identifier = "User $userId",
            rateLimitHours = RATE_LIMIT_HOURS,
            maxGenerations = MAX_GENERATIONS_PER_DAY,
            countFunction = { _, startTime ->
                generatedImageRepository.countByUserIdAndGeneratedAtAfter(userId, startTime)
            },
            rateLimitMessage =
                "Rate limit exceeded. You can generate up to " +
                    "$MAX_GENERATIONS_PER_DAY images per day. Please try again later.",
        )
    }

    override fun processImageGeneration(
        imageFile: MultipartFile,
        request: PublicImageGenerationRequest,
        identifier: String,
    ): PublicImageGenerationResponse {
        val userId = identifier.toLong()

        // Validate user exists
        userQueryService.getUserById(userId)

        // Store the uploaded image first using the new storage pattern
        val uploadedImage = userImageStorageService.storeUploadedImage(imageFile, userId)
        logger.info("Stored uploaded image with UUID: ${uploadedImage.uuid}")

        val openAIRequest = createOpenAIRequest(request)
        logger.debug("Generated OpenAI request: {}", openAIRequest)

        // Generate images using OpenAI service (get raw bytes)
        val imageEditResponse = openAIImageFacade.editImageBytes(imageFile, openAIRequest)

        // Store each generated image using the user storage service (which handles both file and DB operations)
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
    }
}
