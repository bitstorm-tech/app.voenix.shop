package com.jotoai.voenix.shop.openai.internal.service

import com.jotoai.voenix.shop.application.ResourceNotFoundException
import com.jotoai.voenix.shop.image.GeneratedImageDto
import com.jotoai.voenix.shop.image.ImageData
import com.jotoai.voenix.shop.image.ImageException
import com.jotoai.voenix.shop.image.ImageMetadata
import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.image.ImageType
import com.jotoai.voenix.shop.image.UploadedImageDto
import com.jotoai.voenix.shop.image.ValidationRequest
import com.jotoai.voenix.shop.openai.internal.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.openai.internal.dto.ImageEditBytesResponse
import com.jotoai.voenix.shop.openai.internal.dto.ImageEditResponse
import com.jotoai.voenix.shop.openai.internal.dto.ImageGenerationRequest
import com.jotoai.voenix.shop.openai.internal.dto.ImageGenerationResponse
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptRequest
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptResponse
import com.jotoai.voenix.shop.openai.internal.model.ImageBackground
import com.jotoai.voenix.shop.openai.internal.model.ImageQuality
import com.jotoai.voenix.shop.openai.internal.model.ImageSize
import com.jotoai.voenix.shop.openai.internal.provider.GenerationOptions
import com.jotoai.voenix.shop.openai.internal.provider.ImageGenerationProvider
import com.jotoai.voenix.shop.openai.internal.provider.MockImageProvider
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.user.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/**
 * Orchestrator service that delegates image generation to appropriate providers.
 * Handles business logic, rate limiting, and storage while delegating AI provider communication.
 */
@Service
internal class OpenAIImageService(
    @Value($$"${app.test-mode:false}") private val testMode: Boolean,
    private val providers: Map<AiProvider, ImageGenerationProvider>,
    private val mockImageProvider: MockImageProvider,
    private val rateLimitService: RateLimitService,
    private val promptQueryService: PromptQueryService,
    private val imageService: ImageService,
    private val userService: UserService,
) {
    enum class AiProvider {
        OPENAI,
        GOOGLE,
        FLUX,
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    init {
        if (testMode) {
            logger.warn { "======================================================" }
            logger.warn { "TEST MODE ACTIVE - Image generation will return mock data" }
            logger.warn { "Original images will be returned instead of AI-generated ones" }
            logger.warn { "======================================================" }
        } else {
            logger.info { "OpenAI Image Service initialized - Real AI generation active" }
        }
    }

    @Transactional
    fun generatePublicImage(
        request: ImageGenerationRequest,
        ipAddress: String,
        imageFile: MultipartFile,
        provider: AiProvider = AiProvider.OPENAI,
    ): ImageGenerationResponse {
        logger.info { "Generating image: public, prompt=${request.promptId}, provider=$provider" }

        val validation = imageService.validate(ValidationRequest.FileUpload(imageFile))
        if (!validation.valid) {
            throw ImageException.Processing(validation.message ?: "Image validation failed")
        }

        rateLimitService.checkPublicLimit(ipAddress)

        return try {
            processPublicImageGeneration(imageFile, request.promptId, ipAddress, provider)
        } catch (e: ImageException.Storage) {
            handleSystemError(e, "public image generation")
        }
    }

    @Transactional
    fun generateUserImageWithIds(
        promptId: Long,
        uploadedImageUuid: UUID?,
        userId: Long,
        provider: AiProvider = AiProvider.OPENAI,
    ): ImageGenerationResponse {
        logger.info { "Generating image: user=$userId, prompt=$promptId, provider=$provider" }
        requireNotNull(uploadedImageUuid) { "uploadedImageUuid is required for user image generation" }

        userService.getUserById(userId)
        rateLimitService.checkUserLimit(userId)

        return try {
            processUserImageGeneration(uploadedImageUuid, promptId, userId, provider)
        } catch (e: ImageException.Storage) {
            handleSystemError(e, "user image generation")
        }
    }

    fun generateImageBytes(
        imageBytes: ByteArray,
        promptId: Long,
        provider: AiProvider = AiProvider.OPENAI,
    ): List<ByteArray> {
        logger.info { "Generating images with $provider for prompt ID: $promptId" }
        val request =
            CreateImageEditRequest(
                promptId = promptId,
                background = ImageBackground.OPAQUE,
                quality = ImageQuality.MEDIUM,
                size = ImageSize.LANDSCAPE_1536X1024,
                n = 4,
            )

        val multipartFile = ByteArrayMultipartFile(imageBytes, "image.png")
        val response = generateImages(multipartFile, request, provider)
        logger.info { "Successfully generated ${response.imageBytes.size} images with $provider" }
        return response.imageBytes
    }

    fun editImage(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
        provider: AiProvider = AiProvider.OPENAI,
    ): ImageEditResponse {
        logger.info { "Starting image edit request with $provider for prompt ID: ${request.promptId}" }

        val bytesResponse = generateImages(imageFile, request, provider)

        val savedImageFilenames =
            bytesResponse.imageBytes.map { imageBytes ->
                val imageData = ImageData.Bytes(imageBytes, "generated-image.png")
                val metadata = ImageMetadata(type = ImageType.PRIVATE)
                val storedImage = imageService.store(imageData, metadata)
                storedImage.filename
            }

        return ImageEditResponse(imageFilenames = savedImageFilenames)
    }

    fun generateImages(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
        provider: AiProvider = AiProvider.OPENAI,
    ): ImageEditBytesResponse {
        val prompt = promptQueryService.getPromptById(request.promptId)
        val options =
            GenerationOptions(
                size = request.size.apiValue,
                background = request.background.apiValue,
                quality = request.quality.apiValue,
                n = request.n,
            )

        return runBlocking {
            val selectedProvider = if (testMode) mockImageProvider else providers[provider]
            selectedProvider?.generateImages(imageFile, prompt, options)
                ?: error("Provider $provider not available")
        }
    }

    fun testPrompt(
        imageFile: MultipartFile,
        request: TestPromptRequest,
        provider: AiProvider = AiProvider.OPENAI,
    ): TestPromptResponse =
        runBlocking {
            val selectedProvider = if (testMode) mockImageProvider else providers[provider]
            selectedProvider?.testPrompt(imageFile, request)
                ?: error("Provider $provider not available")
        }

    private fun processPublicImageGeneration(
        imageFile: MultipartFile,
        promptId: Long,
        ipAddress: String,
        provider: AiProvider = AiProvider.OPENAI,
    ): ImageGenerationResponse {
        val imageBytes = imageFile.bytes
        val generatedBytes = generateImageBytes(imageBytes, promptId, provider)

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

    private fun processUserImageGeneration(
        uploadedImageUuid: UUID,
        promptId: Long,
        userId: Long,
        provider: AiProvider = AiProvider.OPENAI,
    ): ImageGenerationResponse {
        val uploadedImage = imageService.getUploadedImageByUuid(uploadedImageUuid, userId) as UploadedImageDto
        val imageContent = imageService.get(uploadedImage.filename, userId)
        val imageBytes = imageContent.bytes

        val generatedBytes = generateImageBytes(imageBytes, promptId, provider)

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

    private class ByteArrayMultipartFile(
        private val fileBytes: ByteArray,
        private val fileName: String,
        private val contentType: String = "image/png",
    ) : MultipartFile {
        override fun getName(): String = "file"

        override fun getOriginalFilename(): String = fileName

        override fun getContentType(): String = contentType

        override fun isEmpty(): Boolean = fileBytes.isEmpty()

        override fun getSize(): Long = fileBytes.size.toLong()

        override fun getBytes(): ByteArray = fileBytes

        override fun getInputStream() = fileBytes.inputStream()

        override fun transferTo(dest: java.io.File) {
            dest.writeBytes(fileBytes)
        }
    }

    private fun handleSystemError(
        e: Exception,
        operation: String,
    ): Nothing {
        logger.error(e) { "Error during $operation" }
        throw ImageException.Processing("Failed to generate image. Please try again later.", e)
    }
}
