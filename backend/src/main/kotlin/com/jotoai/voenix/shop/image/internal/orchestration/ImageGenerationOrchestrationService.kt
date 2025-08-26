package com.jotoai.voenix.shop.image.internal.orchestration

import com.jotoai.voenix.shop.common.exception.BadRequestException
import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.image.api.ImageStorageService
import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.openai.api.OpenAIImageGenerationService
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.user.api.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional(readOnly = true)
class ImageGenerationOrchestrationService(
    private val openAIImageGenerationService: OpenAIImageGenerationService,
    private val imageFacade: ImageFacade,
    private val imageStorageService: ImageStorageService,
    private val storagePathService: StoragePathService,
    private val userService: UserService,
    private val promptQueryService: PromptQueryService,
) {
    companion object {
        private val logger = KotlinLogging.logger {}

        private const val PUBLIC_RATE_LIMIT_HOURS = 1
        private const val PUBLIC_MAX_GENERATIONS_PER_HOUR = 10
        private const val USER_RATE_LIMIT_HOURS = 24
        private const val USER_MAX_GENERATIONS_PER_DAY = 50
    }

    @Transactional
    fun generatePublicImage(
        request: PublicImageGenerationRequest,
        ipAddress: String,
        imageFile: MultipartFile,
    ): PublicImageGenerationResponse {
        logger.info { "Generating image: public, prompt=${request.promptId}" }

        imageFacade.validateImageFile(imageFile)
        validatePrompt(request.promptId)

        val hourAgo = LocalDateTime.now().minusHours(PUBLIC_RATE_LIMIT_HOURS.toLong())
        val count = imageFacade.countGeneratedImagesForIpAfter(ipAddress, hourAgo)
        checkRateLimit(
            count,
            PUBLIC_MAX_GENERATIONS_PER_HOUR,
            "Rate limit exceeded. Max $PUBLIC_MAX_GENERATIONS_PER_HOUR images per hour.",
        )

        try {
            return processImageGeneration(
                ImageSource.File(imageFile),
                request.promptId,
                null,
                ipAddress,
                request.cropArea,
            )
        } catch (e: BadRequestException) {
            throw e
        } catch (e: Exception) {
            handleSystemError(e, "public image generation")
        }
    }

    @Transactional
    fun generateUserImageWithIds(
        promptId: Long,
        uploadedImageUuid: UUID,
        userId: Long,
        cropArea: CropArea?,
    ): PublicImageGenerationResponse {
        logger.info { "Generating image: user=$userId, prompt=$promptId" }

        validatePrompt(promptId)
        userService.getUserById(userId)

        val dayAgo = LocalDateTime.now().minusHours(USER_RATE_LIMIT_HOURS.toLong())
        val count = imageFacade.countGeneratedImagesForUserAfter(userId, dayAgo)
        checkRateLimit(
            count,
            USER_MAX_GENERATIONS_PER_DAY,
            "Rate limit exceeded. Max $USER_MAX_GENERATIONS_PER_DAY images per day.",
        )

        try {
            return processImageGeneration(
                ImageSource.Stored(uploadedImageUuid),
                promptId,
                userId,
                null,
                cropArea,
            )
        } catch (e: BadRequestException) {
            throw e
        } catch (e: Exception) {
            handleSystemError(e, "user image generation")
        }
    }

    private fun validatePrompt(promptId: Long) {
        val prompt = promptQueryService.getPromptById(promptId)
        if (!prompt.active) {
            throw BadRequestException("The selected prompt is not available")
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

    private fun processImageGeneration(
        source: ImageSource,
        promptId: Long,
        userId: Long?,
        ipAddress: String?,
        cropArea: CropArea?,
    ): PublicImageGenerationResponse {
        val imageBytes =
            when (source) {
                is ImageSource.File -> {
                    val storedFilename =
                        if (cropArea != null) {
                            imageStorageService.storeFile(source.file, ImageType.PUBLIC, cropArea)
                        } else {
                            imageStorageService.storeFile(source.file, ImageType.PUBLIC)
                        }
                    imageStorageService.loadFileAsBytes(storedFilename, ImageType.PUBLIC)
                }
                is ImageSource.Stored -> {
                    val uploadedImage = imageFacade.getUploadedImageByUuid(source.uuid, userId!!)
                    if (cropArea != null) {
                        val originalBytes = imageStorageService.loadFileAsBytes(uploadedImage.filename, ImageType.PRIVATE)
                        val croppedFilename =
                            imageStorageService.storeFile(
                                originalBytes,
                                uploadedImage.originalFilename,
                                ImageType.PRIVATE,
                            )
                        imageStorageService.loadFileAsBytes(croppedFilename, ImageType.PRIVATE)
                    } else {
                        imageStorageService.loadFileAsBytes(uploadedImage.filename, ImageType.PRIVATE)
                    }
                }
            }

        val generatedBytes = openAIImageGenerationService.generateImages(imageBytes, promptId)

        val generatedImages =
            generatedBytes.mapIndexed { index, bytes ->
                if (userId != null) {
                    val uploadedImage = imageFacade.getUploadedImageByUuid((source as ImageSource.Stored).uuid, userId)
                    imageFacade.storeGeneratedImage(
                        imageBytes = bytes,
                        uploadedImageId = uploadedImage.id!!,
                        promptId = promptId,
                        generationNumber = index + 1,
                    )
                } else {
                    imageFacade.storePublicGeneratedImage(
                        imageBytes = bytes,
                        promptId = promptId,
                        ipAddress = ipAddress!!,
                        generationNumber = index + 1,
                    )
                }
            }

        val imageUrls =
            if (userId != null) {
                val uploadedImage = imageFacade.getUploadedImageByUuid((source as ImageSource.Stored).uuid, userId)
                generatedImages.mapIndexed { index, _ ->
                    "/api/user/images/${uploadedImage.uuid}_generated_${index + 1}.png"
                }
            } else {
                generatedImages.map { generatedImageDto ->
                    storagePathService.getImageUrl(ImageType.PUBLIC, generatedImageDto.filename)
                }
            }

        val imageIds = generatedImages.mapNotNull { it.id }
        logger.info { "Generated ${imageIds.size} images" }

        return PublicImageGenerationResponse(
            imageUrls = imageUrls,
            generatedImageIds = imageIds,
        )
    }

    private fun handleSystemError(
        e: Exception,
        operation: String,
    ): Nothing {
        logger.error(e) { "Error during $operation" }
        throw RuntimeException("Failed to generate image. Please try again later.", e)
    }
}

sealed class ImageSource {
    data class File(
        val file: MultipartFile,
    ) : ImageSource()

    data class Stored(
        val uuid: UUID,
    ) : ImageSource()
}