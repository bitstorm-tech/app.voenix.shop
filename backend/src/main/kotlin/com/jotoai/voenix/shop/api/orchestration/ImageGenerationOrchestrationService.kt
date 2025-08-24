package com.jotoai.voenix.shop.api.orchestration

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
import java.io.IOException
import java.time.LocalDateTime
import java.util.*
import org.springframework.core.io.Resource
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

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
        logger.info { "Processing public image generation request for prompt ID: ${request.promptId}" }

        imageFacade.validateImageFile(imageFile)
        checkPublicRateLimit(ipAddress)
        validatePrompt(request.promptId)

        return executeWithErrorHandling(
            operation = { processPublicImageGeneration(imageFile, request, ipAddress) },
            contextMessage = "generating image for public user with IP: $ipAddress",
        )
    }

    @Transactional
    fun generateUserImageWithIds(
        promptId: Long,
        uploadedImageUuid: UUID?,
        userId: Long,
        cropArea: CropArea?,
    ): PublicImageGenerationResponse {
        logger.info { "Processing user image generation with IDs request for user $userId with prompt ID: $promptId" }

        requireNotNull(uploadedImageUuid) { "Uploaded image UUID is required for user image generation" }

        checkUserRateLimit(userId)
        validatePrompt(promptId)
        userService.getUserById(userId)

        return executeWithErrorHandling(
            operation = { processUserImageGenerationWithIds(promptId, uploadedImageUuid, userId, cropArea) },
            contextMessage = "generating image with IDs for user $userId",
        )
    }

    private fun processPublicImageGeneration(
        imageFile: MultipartFile,
        request: PublicImageGenerationRequest,
        ipAddress: String,
    ): PublicImageGenerationResponse {
        logger.debug { "Processing public image generation for IP: $ipAddress" }

        val storedFilename = if (request.cropArea != null) {
            logger.info { "Storing cropped image with area: ${request.cropArea}" }
            imageStorageService.storeFile(imageFile, ImageType.PUBLIC, request.cropArea)
        } else {
            imageStorageService.storeFile(imageFile, ImageType.PUBLIC)
        }

        val storedImageResource = imageStorageService.loadFileAsResource(storedFilename, ImageType.PUBLIC)
        val processedImageFile = ResourceMultipartFile(
            resource = storedImageResource,
            filename = imageFile.originalFilename ?: "image.png",
            contentType = imageFile.contentType ?: "image/png",
        )

        val imageBytes = openAIImageGenerationService.generateImages(processedImageFile, request)

        val generatedImages = imageBytes.mapIndexed { index, bytes ->
            imageFacade.storePublicGeneratedImage(
                imageBytes = bytes,
                promptId = request.promptId,
                ipAddress = ipAddress,
                generationNumber = index + 1,
            )
        }

        val imageUrls = generatedImages.map { generatedImageDto ->
            storagePathService.getImageUrl(ImageType.PUBLIC, generatedImageDto.filename)
        }

        val imageIds = generatedImages.map { generatedImageDto -> generatedImageDto.id!! }

        logger.info { "Successfully generated ${imageUrls.size} images for public user with IP: $ipAddress" }

        return PublicImageGenerationResponse(
            imageUrls = imageUrls,
            generatedImageIds = imageIds,
        )
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

        val uploadedImage = imageFacade.getUploadedImageByUuid(uploadedImageUuid, userId)

        val originalImageResource = imageStorageService.loadFileAsResource(uploadedImage.filename, ImageType.PRIVATE)
        val originalMultipartFile = ResourceMultipartFile(
            resource = originalImageResource,
            filename = uploadedImage.originalFilename,
            contentType = "image/png",
        )

        val processedFilename = if (cropArea != null) {
            logger.info { "Storing cropped image with area: $cropArea for user $userId (with IDs)" }
            imageStorageService.storeFile(originalMultipartFile, ImageType.PRIVATE, cropArea)
        } else {
            uploadedImage.filename
        }

        val processedImageResource = imageStorageService.loadFileAsResource(processedFilename, ImageType.PRIVATE)
        val multipartFile = ResourceMultipartFile(
            resource = processedImageResource,
            filename = uploadedImage.originalFilename,
            contentType = "image/png",
        )

        val generatedImageBytes = openAIImageGenerationService.generateImages(multipartFile, promptId)

        val generatedImages = generatedImageBytes.mapIndexed { index, generatedBytes ->
            logger.info { "Processing generated image ${index + 1} of ${generatedImageBytes.size}" }
            val generatedImage = imageFacade.storeGeneratedImage(
                imageBytes = generatedBytes,
                uploadedImageId = uploadedImage.id!!,
                promptId = promptId,
                generationNumber = index + 1,
            )
            logger.info {
                "Saved generated image to database with ID: ${generatedImage.id}, " +
                    "filename: ${generatedImage.filename}"
            }
            generatedImage
        }

        val baseUuid = uploadedImage.uuid.toString()
        val imageUrls = (1..4).map { index ->
            "/api/user/images/${baseUuid}_generated_$index.png"
        }

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
                imageFacade.countGeneratedImagesForIpAfter(ipAddress, startTime)
            },
            rateLimitMessage = "Rate limit exceeded. You can generate up to " +
                "$PUBLIC_MAX_GENERATIONS_PER_HOUR images per hour. Please try again later.",
        )
    }

    private fun checkUserRateLimit(userId: Long) {
        checkTimeBasedRateLimit(
            identifier = "User $userId",
            rateLimitHours = USER_RATE_LIMIT_HOURS,
            maxGenerations = USER_MAX_GENERATIONS_PER_DAY,
            countFunction = { _, startTime ->
                imageFacade.countGeneratedImagesForUserAfter(userId, startTime)
            },
            rateLimitMessage = "Rate limit exceeded. You can generate up to " +
                "$USER_MAX_GENERATIONS_PER_DAY images per day. Please try again later.",
        )
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