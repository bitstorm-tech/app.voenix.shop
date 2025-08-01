package com.jotoai.voenix.shop.domain.openai.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.jotoai.voenix.shop.domain.openai.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.domain.openai.dto.ImageEditBytesResponse
import com.jotoai.voenix.shop.domain.openai.dto.TestPromptRequest
import com.jotoai.voenix.shop.domain.openai.dto.TestPromptRequestParams
import com.jotoai.voenix.shop.domain.openai.dto.TestPromptResponse
import com.jotoai.voenix.shop.domain.prompts.dto.PromptDto
import com.jotoai.voenix.shop.domain.prompts.service.PromptService
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
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
import io.ktor.client.statement.readBytes
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.jackson.jackson
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import kotlin.io.encoding.Base64

/**
 * OpenAI-based implementation of image generation strategy.
 * Uses the OpenAI API to generate and edit images.
 */
@Service
@ConditionalOnProperty(name = ["app.test-mode"], havingValue = "false", matchIfMissing = true)
class OpenAIImageGenerationStrategy(
    @param:Value("\${OPENAI_API_KEY}") private val apiKey: String,
    private val promptService: PromptService,
) : ImageGenerationStrategy {
    companion object {
        private val logger = LoggerFactory.getLogger(OpenAIImageGenerationStrategy::class.java)
        private const val OPENAI_API_URL = "https://api.openai.com/v1/images/edits"
    }

    init {
        logger.info("OpenAI Image Generation Strategy initialized - Real AI generation active")
    }

    private val httpClient =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                jackson()
            }
            install(Logging) {
                logger = io.ktor.client.plugins.logging.Logger.SIMPLE
                level = LogLevel.INFO
            }
            engine {
                requestTimeout = 300000 // 5 minutes
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
        @param:JsonProperty("b64_json") val b64Json: String? = null,
        @param:JsonProperty("revised_prompt") val revisedPrompt: String? = null,
    )

    /**
     * Calls the OpenAI image edit API and returns the raw response data.
     * This is the shared implementation used by both public edit methods and test prompt.
     */
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
                        append(HttpHeaders.ContentType, getContentType(imageFile.originalFilename ?: "image.png"))
                        append(HttpHeaders.ContentDisposition, "filename=\"${imageFile.originalFilename ?: "image.png"}\"")
                    },
                )

                append("prompt", promptText)
                append("n", n.toString())
                append("size", size)
                background?.let { append("background", it) }
                responseFormat?.let { append("response_format", it) }
            }

        val httpResponse =
            httpClient
                .post(OPENAI_API_URL) {
                    header(HttpHeaders.Authorization, "Bearer $apiKey")
                    setBody(MultiPartFormDataContent(formData))
                }

        if (!httpResponse.status.isSuccess()) {
            val errorBody = httpResponse.bodyAsText()
            logger.error("OpenAI API returned error status ${httpResponse.status}: $errorBody")
            throw RuntimeException("OpenAI API error: ${httpResponse.status} - $errorBody")
        }

        val response = httpResponse.body<OpenAIResponse>()
        logger.info("Successfully received response from OpenAI API")

        if (response.error != null) {
            logger.error("OpenAI API returned error: ${response.error.message}")
            throw RuntimeException("OpenAI API error: ${response.error.message}")
        }

        if (response.data.isNullOrEmpty()) {
            logger.error("OpenAI API returned empty or null data")
            throw RuntimeException("OpenAI API returned no images")
        }

        return response
    }

    /**
     * Extracts image bytes from OpenAI response data.
     */
    private suspend fun extractImageBytes(responseData: List<OpenAIImage>): List<ByteArray> =
        responseData.map { openAIImage: OpenAIImage ->
            when {
                openAIImage.url != null -> {
                    // Download image from URL
                    logger.debug("Downloading image from URL: ${openAIImage.url}")
                    httpClient.get(openAIImage.url).readBytes()
                }
                openAIImage.b64Json != null -> {
                    // Decode base64 image
                    logger.debug("Decoding base64 image")
                    Base64.decode(openAIImage.b64Json)
                }
                else -> {
                    throw IllegalStateException("OpenAI response contains neither URL nor base64 data")
                }
            }
        }

    override fun generateImages(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
    ): ImageEditBytesResponse =
        runBlocking {
            logger.info("Starting OpenAI image generation with prompt ID: ${request.promptId}")

            val prompt = promptService.getPromptById(request.promptId)

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
                logger.error("Error during OpenAI API call", e)
                throw RuntimeException("Failed to generate images: ${e.message}", e)
            }
        }

    override fun testPrompt(
        imageFile: MultipartFile,
        request: TestPromptRequest,
    ): TestPromptResponse =
        runBlocking {
            logger.info("Starting OpenAI prompt test with master prompt: ${request.masterPrompt}")

            try {
                val combinedPrompt = "${request.masterPrompt} ${request.specificPrompt}".trim()

                val response =
                    callOpenAIEditAPI(
                        imageFile = imageFile,
                        promptText = combinedPrompt,
                        size = request.size.apiValue,
                        background = null, // testPrompt doesn't use background in API call
                        n = 1,
                        responseFormat = "url",
                    )

                val openAIImage = response.data!!.first()
                val imageUrl = openAIImage.url ?: throw RuntimeException("No image URL returned")

                TestPromptResponse(
                    imageUrl = imageUrl,
                    requestParams =
                        TestPromptRequestParams(
                            model = "dall-e-2",
                            size = request.size.apiValue,
                            n = 1,
                            responseFormat = "url",
                            masterPrompt = request.masterPrompt,
                            specificPrompt = request.specificPrompt,
                            combinedPrompt = combinedPrompt,
                            quality = request.quality.name.lowercase(),
                            background = request.background.name.lowercase(),
                        ),
                )
            } catch (e: Exception) {
                logger.error("Error during OpenAI API call", e)
                throw RuntimeException("Failed to test prompt: ${e.message}", e)
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
}
