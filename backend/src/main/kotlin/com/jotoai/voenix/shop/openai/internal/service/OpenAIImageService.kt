package com.jotoai.voenix.shop.openai.internal.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonProcessingException
import com.jotoai.voenix.shop.application.BadRequestException
import com.jotoai.voenix.shop.application.ResourceNotFoundException
import com.jotoai.voenix.shop.image.CountFilter
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
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptRequestParams
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptResponse
import com.jotoai.voenix.shop.openai.internal.exception.ImageGenerationException
import com.jotoai.voenix.shop.openai.internal.model.ImageBackground
import com.jotoai.voenix.shop.openai.internal.model.ImageQuality
import com.jotoai.voenix.shop.openai.internal.model.ImageSize
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto
import com.jotoai.voenix.shop.user.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.jackson.jackson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.LocalDateTime
import java.util.UUID
import kotlin.io.encoding.Base64

/**
 * Consolidated OpenAI image service that handles all image generation functionality including:
 * - OpenAI API communication (production and test modes)
 * - Rate limiting for public and user requests
 * - Image storage and URL generation
 * - Prompt testing and validation
 */
@Service
class OpenAIImageService(
    @Value("\${app.test-mode:false}") private val testMode: Boolean,
    @Value("\${OPENAI_API_KEY:}") private val apiKey: String,
    private val promptQueryService: PromptQueryService,
    private val imageService: ImageService,
    private val userService: UserService,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val OPENAI_API_URL = "https://api.openai.com/v1/images/edits"
        private const val REQUEST_TIMEOUT_MS = 300000L // 5 minutes in milliseconds
        private const val PUBLIC_RATE_LIMIT_HOURS = 1
        private const val PUBLIC_MAX_GENERATIONS_PER_HOUR = 10
        private const val USER_RATE_LIMIT_HOURS = 24
        private const val USER_MAX_GENERATIONS_PER_DAY = 50
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

    private val httpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                jackson()
            }
            install(Logging) {
                logger = io.ktor.client.plugins.logging.Logger.SIMPLE
                level = LogLevel.INFO
            }
            engine {
                requestTimeout = REQUEST_TIMEOUT_MS
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class OpenAIResponse(
        val data: List<OpenAIImage>? = null,
        val error: OpenAIError? = null,
    )

    data class OpenAIError(
        val message: String,
        val type: String? = null,
        val param: String? = null,
        val code: String? = null,
    )

    data class OpenAIImage(
        val url: String? = null,
        @JsonProperty("b64_json") val b64Json: String? = null,
        @JsonProperty("revised_prompt") val revisedPrompt: String? = null,
    )

    @Transactional
    fun generatePublicImage(
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
            return processPublicImageGeneration(imageFile, request.promptId, ipAddress)
        } catch (e: ImageException.Storage) {
            handleSystemError(e, "public image generation")
        }
    }

    @Transactional
    fun generateUserImageWithIds(
        promptId: Long,
        uploadedImageUuid: UUID?,
        userId: Long,
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
            return processUserImageGeneration(uploadedImageUuid, promptId, userId)
        } catch (e: ImageException.Storage) {
            handleSystemError(e, "user image generation")
        }
    }

    fun generateImageBytes(
        imageBytes: ByteArray,
        promptId: Long,
    ): List<ByteArray> {
        logger.info { "Generating images with OpenAI for prompt ID: $promptId" }
        val request =
            CreateImageEditRequest(
                promptId = promptId,
                background = ImageBackground.OPAQUE,
                quality = ImageQuality.MEDIUM,
                size = ImageSize.LANDSCAPE_1536X1024,
                n = 4,
            )

        val multipartFile = ByteArrayMultipartFile(imageBytes, "image.png")
        val response = generateImages(multipartFile, request)
        logger.info { "Successfully generated ${response.imageBytes.size} images with OpenAI" }
        return response.imageBytes
    }

    fun editImage(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
    ): ImageEditResponse {
        logger.info { "Starting image edit request with prompt ID: ${request.promptId}" }

        val bytesResponse = generateImages(imageFile, request)

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
    ): ImageEditBytesResponse {
        val prompt = promptQueryService.getPromptById(request.promptId)

        return if (testMode) {
            generateTestModeImages(imageFile, request, prompt)
        } else {
            generateRealImages(imageFile, request, prompt)
        }
    }

    fun testPrompt(
        imageFile: MultipartFile,
        request: TestPromptRequest,
    ): TestPromptResponse =
        if (testMode) {
            generateTestPromptResponse(request)
        } else {
            generateRealPromptResponse(imageFile, request)
        }

    private fun generateTestModeImages(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
        prompt: PromptDto,
    ): ImageEditBytesResponse {
        logger.info { "TEST MODE: Generating ${request.n} mock images for prompt ID: ${request.promptId}" }
        logger.debug { "TEST MODE: Using prompt '${prompt.promptText}' for mock generation" }

        // Return the original image N times
        val originalImageBytes = imageFile.bytes
        val mockImageList =
            (1..request.n).map {
                logger.debug { "TEST MODE: Creating mock image $it of ${request.n}" }
                originalImageBytes.copyOf() // Create a copy to avoid reference issues
            }

        logger.info { "TEST MODE: Successfully generated ${mockImageList.size} mock images" }
        return ImageEditBytesResponse(imageBytes = mockImageList)
    }

    private fun generateRealImages(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
        prompt: PromptDto,
    ): ImageEditBytesResponse =
        runBlocking {
            logger.info { "Starting OpenAI image generation with prompt ID: ${request.promptId}" }

            try {
                val response =
                    callOpenAIEditAPI(
                        imageFile = imageFile,
                        promptText = buildFinalPrompt(prompt),
                        size = request.size.apiValue,
                        background = request.background.apiValue,
                        n = request.n,
                    )

                val imageBytesList = extractImageBytes(response.data!!)
                ImageEditBytesResponse(imageBytes = imageBytesList)
            } catch (e: Exception) {
                handleApiError(e, "Image generation")
            }
        }

    private fun generateTestPromptResponse(request: TestPromptRequest): TestPromptResponse {
        logger.info { "TEST MODE: Testing prompt with master prompt: ${request.masterPrompt}" }

        val combinedPrompt = "${request.masterPrompt} ${request.specificPrompt}".trim()
        val mockImageUrl = "https://test-mode.voenix.shop/images/mock-${UUID.randomUUID()}.png"

        logger.info { "TEST MODE: Generated mock image URL: $mockImageUrl" }

        return TestPromptResponse(
            imageUrl = mockImageUrl,
            requestParams =
                TestPromptRequestParams(
                    model = "test-mode-mock",
                    size = request.getSize().apiValue,
                    n = 1,
                    responseFormat = "url",
                    masterPrompt = request.masterPrompt,
                    specificPrompt = request.specificPrompt,
                    combinedPrompt = combinedPrompt,
                    quality = request.getQuality().apiValue,
                    background = request.getBackground().apiValue,
                ),
        )
    }

    private fun generateRealPromptResponse(
        imageFile: MultipartFile,
        request: TestPromptRequest,
    ): TestPromptResponse =
        runBlocking {
            logger.info { "Starting OpenAI prompt test with master prompt: ${request.masterPrompt}" }

            try {
                val combinedPrompt = "${request.masterPrompt} ${request.specificPrompt}".trim()

                val response =
                    callOpenAIEditAPI(
                        imageFile = imageFile,
                        promptText = combinedPrompt,
                        size = request.getSize().apiValue,
                        background = null,
                        n = 1,
                        responseFormat = "url",
                    )

                val openAIImage = response.data!!.first()
                val imageUrl = openAIImage.url ?: error("No image URL returned")

                TestPromptResponse(
                    imageUrl = imageUrl,
                    requestParams =
                        TestPromptRequestParams(
                            model = "dall-e-2",
                            size = request.getSize().apiValue,
                            n = 1,
                            responseFormat = "url",
                            masterPrompt = request.masterPrompt,
                            specificPrompt = request.specificPrompt,
                            combinedPrompt = combinedPrompt,
                            quality = request.getQuality().apiValue,
                            background = request.getBackground().apiValue,
                        ),
                )
            } catch (e: Exception) {
                handleApiError(e, "Prompt testing")
            }
        }

    private suspend fun callOpenAIEditAPI(
        imageFile: MultipartFile,
        promptText: String,
        size: String,
        background: String? = null,
        n: Int = 1,
        responseFormat: String? = null,
    ): OpenAIResponse {
        val formData =
            formData {
                append("model", "gpt-image-1")
                append(
                    "image",
                    imageFile.bytes,
                    Headers.build {
                        val contentFilename = imageFile.originalFilename ?: "image.png"
                        append(HttpHeaders.ContentType, getContentType(contentFilename))
                        append(
                            HttpHeaders.ContentDisposition,
                            "filename=\"${imageFile.originalFilename ?: "image.png"}\"",
                        )
                    },
                )

                append("prompt", promptText)
                append("n", n.toString())
                append("size", size)
                background?.let { append("background", it) }
                responseFormat?.let { append("response_format", it) }
            }

        val httpResponse =
            httpClient.post(OPENAI_API_URL) {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                setBody(MultiPartFormDataContent(formData))
            }

        if (!httpResponse.status.isSuccess()) {
            val errorBody = httpResponse.bodyAsText()
            logger.error { "OpenAI API returned error status ${httpResponse.status}: $errorBody" }
            error("OpenAI API error: ${httpResponse.status} - $errorBody")
        }

        val response = httpResponse.body<OpenAIResponse>()
        logger.info { "Successfully received response from OpenAI API" }

        validateOpenAIResponse(response)
        return response
    }

    private fun validateOpenAIResponse(response: OpenAIResponse) {
        if (response.error != null) {
            logger.error { "OpenAI API returned error: ${response.error.message}" }
            error("OpenAI API error: ${response.error.message}")
        }

        if (response.data.isNullOrEmpty()) {
            logger.error { "OpenAI API returned empty or null data" }
            error("OpenAI API returned no images")
        }
    }

    private suspend fun extractImageBytes(responseData: List<OpenAIImage>): List<ByteArray> =
        responseData.map { openAIImage ->
            when {
                openAIImage.url != null -> {
                    logger.debug { "Downloading image from URL: ${openAIImage.url}" }
                    httpClient.get(openAIImage.url).body()
                }
                openAIImage.b64Json != null -> {
                    logger.debug { "Decoding base64 image" }
                    Base64.decode(openAIImage.b64Json)
                }
                else -> {
                    error("OpenAI response contains neither URL nor base64 data")
                }
            }
        }

    private fun handleApiError(
        e: Exception,
        operation: String,
    ): Nothing {
        logger.error(e) { "$operation failed" }
        when (e) {
            is CancellationException -> throw e
            is HttpRequestTimeoutException, is SocketTimeoutException ->
                throw ImageGenerationException("$operation timed out", e)
            is ClientRequestException, is ServerResponseException, is ResponseException ->
                throw ImageGenerationException("$operation failed: HTTP error", e)
            is JsonProcessingException ->
                throw ImageGenerationException("$operation failed: JSON processing error", e)
            is IOException ->
                throw ImageGenerationException("$operation failed: IO error", e)
            else ->
                throw ImageGenerationException("$operation failed: ${e.message}", e)
        }
    }

    private fun getContentType(fileName: String): String =
        when {
            fileName.endsWith(".png", ignoreCase = true) -> "image/png"
            fileName.endsWith(".jpg", ignoreCase = true) ||
                fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
            else -> "application/octet-stream"
        }

    private fun buildFinalPrompt(prompt: PromptDto): String {
        val parts = mutableListOf<String>()

        prompt.promptText?.let { parts.add(it) }
        prompt.slots
            .sortedBy { it.promptSlotType?.position ?: 0 }
            .forEach { slot ->
                slot.prompt?.let { parts.add(it) }
            }

        return parts.joinToString(" ")
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

    private fun processPublicImageGeneration(
        imageFile: MultipartFile,
        promptId: Long,
        ipAddress: String,
    ): ImageGenerationResponse {
        val imageBytes = imageFile.bytes
        val generatedBytes = generateImageBytes(imageBytes, promptId)

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
    ): ImageGenerationResponse {
        val uploadedImage = imageService.getUploadedImageByUuid(uploadedImageUuid, userId) as UploadedImageDto
        val imageContent = imageService.get(uploadedImage.filename, userId)
        val imageBytes = imageContent.bytes

        val generatedBytes = generateImageBytes(imageBytes, promptId)

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

    private fun handleSystemError(
        e: Exception,
        operation: String,
    ): Nothing {
        logger.error(e) { "Error during $operation" }
        throw ImageException.Processing("Failed to generate image. Please try again later.", e)
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
}
