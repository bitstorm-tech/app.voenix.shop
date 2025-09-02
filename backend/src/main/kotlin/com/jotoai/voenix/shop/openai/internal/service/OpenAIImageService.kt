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
import java.time.OffsetDateTime
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
    @Value("\${GOOGLE_API_KEY:}") private val googleApiKey: String,
    @Value("\${FLUX_API_KEY:}") private val fluxApiKey: String,
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
        provider: AiProvider = AiProvider.OPENAI,
    ): ImageGenerationResponse {
        logger.info { "Generating image: public, prompt=${request.promptId}, provider=$provider" }

        val validation = imageService.validate(ValidationRequest.FileUpload(imageFile))
        if (!validation.valid) {
            throw ImageException.Processing(validation.message ?: "Image validation failed")
        }

        val hourAgo =
            OffsetDateTime
                .now()
                .minusHours(PUBLIC_RATE_LIMIT_HOURS.toLong())
        val count = imageService.count(CountFilter(ipAddress = ipAddress, after = hourAgo))
        checkRateLimit(
            count,
            PUBLIC_MAX_GENERATIONS_PER_HOUR,
            "Rate limit exceeded. Max $PUBLIC_MAX_GENERATIONS_PER_HOUR images per hour.",
        )

        try {
            return processPublicImageGeneration(imageFile, request.promptId, ipAddress, provider)
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

        val dayAgo =
            OffsetDateTime
                .now()
                .minusHours(USER_RATE_LIMIT_HOURS.toLong())
        val count = imageService.count(CountFilter(userId = userId, after = dayAgo))
        checkRateLimit(
            count,
            USER_MAX_GENERATIONS_PER_DAY,
            "Rate limit exceeded. Max $USER_MAX_GENERATIONS_PER_DAY images per day.",
        )

        try {
            return processUserImageGeneration(uploadedImageUuid, promptId, userId, provider)
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

        return if (testMode) {
            generateTestModeImages(imageFile, request, prompt)
        } else {
            when (provider) {
                AiProvider.OPENAI -> generateWithOpenAI(imageFile, request, prompt)
                AiProvider.GOOGLE -> generateWithGoogle(imageFile, request, prompt)
                AiProvider.FLUX -> generateWithFlux(imageFile, request, prompt)
            }
        }
    }

    fun testPrompt(
        imageFile: MultipartFile,
        request: TestPromptRequest,
        provider: AiProvider = AiProvider.OPENAI,
    ): TestPromptResponse =
        if (testMode) {
            generateTestPromptResponse(request)
        } else {
            when (provider) {
                AiProvider.OPENAI -> generateRealPromptResponse(imageFile, request)
                AiProvider.GOOGLE -> generateGooglePromptResponse(imageFile, request)
                AiProvider.FLUX -> generateFluxPromptResponse(imageFile, request)
            }
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

    private fun generateWithOpenAI(
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
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpRequestTimeoutException) {
                handleApiError(e, "Image generation")
            } catch (e: SocketTimeoutException) {
                handleApiError(e, "Image generation")
            } catch (e: ResponseException) {
                handleApiError(e, "Image generation")
            } catch (e: JsonProcessingException) {
                handleApiError(e, "Image generation")
            } catch (e: IOException) {
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
            } catch (e: CancellationException) {
                throw e
            } catch (e: HttpRequestTimeoutException) {
                handleApiError(e, "Prompt testing")
            } catch (e: SocketTimeoutException) {
                handleApiError(e, "Prompt testing")
            } catch (e: ResponseException) {
                handleApiError(e, "Prompt testing")
            } catch (e: JsonProcessingException) {
                handleApiError(e, "Prompt testing")
            } catch (e: IOException) {
                handleApiError(e, "Prompt testing")
            }
        }

    private fun generateWithGoogle(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
        prompt: PromptDto,
    ): ImageEditBytesResponse =
        runBlocking {
            logger.info { "Starting Google AI image generation with prompt ID: ${request.promptId}" }

            if (googleApiKey.isEmpty()) {
                throw ImageGenerationException("Google API key not configured")
            }

            try {
                // Google Imagen API implementation
                // For now, we'll create a placeholder that mimics the response structure
                // In production, this would call Google's Imagen API
                val promptText = buildFinalPrompt(prompt)

                // Placeholder for Google Imagen API implementation
                // This will be implemented when Google API credentials are available
                logger.warn { "Google provider implementation pending - using mock response" }

                // Return mock data for now
                val mockImageBytes = imageFile.bytes
                val imageList = (1..request.n).map { mockImageBytes.copyOf() }

                ImageEditBytesResponse(imageBytes = imageList)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleApiError(e, "Google image generation")
            }
        }

    private fun generateWithFlux(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
        prompt: PromptDto,
    ): ImageEditBytesResponse =
        runBlocking {
            logger.info { "Starting Flux AI image generation with prompt ID: ${request.promptId}" }

            if (fluxApiKey.isEmpty()) {
                throw ImageGenerationException("Flux API key not configured")
            }

            try {
                // Flux API implementation
                // For now, we'll create a placeholder that mimics the response structure
                // In production, this would call Flux's API
                val promptText = buildFinalPrompt(prompt)

                // Placeholder for Flux API implementation
                // This will be implemented when Flux API credentials are available
                logger.warn { "Flux provider implementation pending - using mock response" }

                // Return mock data for now
                val mockImageBytes = imageFile.bytes
                val imageList = (1..request.n).map { mockImageBytes.copyOf() }

                ImageEditBytesResponse(imageBytes = imageList)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleApiError(e, "Flux image generation")
            }
        }

    private fun generateGooglePromptResponse(
        @Suppress("UNUSED_PARAMETER") imageFile: MultipartFile,
        request: TestPromptRequest,
    ): TestPromptResponse =
        runBlocking {
            logger.info { "Starting Google AI prompt test with master prompt: ${request.masterPrompt}" }

            if (googleApiKey.isEmpty()) {
                throw ImageGenerationException("Google API key not configured")
            }

            try {
                val combinedPrompt = "${request.masterPrompt} ${request.specificPrompt}".trim()

                // Placeholder for Google Imagen API prompt testing
                logger.warn { "Google provider prompt test implementation pending - using mock response" }

                val mockImageUrl = "https://test-mode.voenix.shop/images/google-mock-${UUID.randomUUID()}.png"

                TestPromptResponse(
                    imageUrl = mockImageUrl,
                    requestParams =
                        TestPromptRequestParams(
                            model = "imagen-2",
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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleApiError(e, "Google prompt testing")
            }
        }

    private fun generateFluxPromptResponse(
        @Suppress("UNUSED_PARAMETER") imageFile: MultipartFile,
        request: TestPromptRequest,
    ): TestPromptResponse =
        runBlocking {
            logger.info { "Starting Flux AI prompt test with master prompt: ${request.masterPrompt}" }

            if (fluxApiKey.isEmpty()) {
                throw ImageGenerationException("Flux API key not configured")
            }

            try {
                val combinedPrompt = "${request.masterPrompt} ${request.specificPrompt}".trim()

                // Placeholder for Flux API prompt testing
                logger.warn { "Flux provider prompt test implementation pending - using mock response" }

                val mockImageUrl = "https://test-mode.voenix.shop/images/flux-mock-${UUID.randomUUID()}.png"

                TestPromptResponse(
                    imageUrl = mockImageUrl,
                    requestParams =
                        TestPromptRequestParams(
                            model = "flux-1",
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
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleApiError(e, "Flux prompt testing")
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
