package com.jotoai.voenix.shop.openai.internal.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonProcessingException
import com.jotoai.voenix.shop.openai.api.ImageGenerationStrategy
import com.jotoai.voenix.shop.openai.api.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.openai.api.dto.ImageEditBytesResponse
import com.jotoai.voenix.shop.openai.api.dto.TestPromptRequest
import com.jotoai.voenix.shop.openai.api.dto.TestPromptRequestParams
import com.jotoai.voenix.shop.openai.api.dto.TestPromptResponse
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.io.encoding.Base64

/**
 * OpenAI-based implementation of image generation strategy.
 * Uses the OpenAI API to generate and edit images.
 */
@Service
@ConditionalOnProperty(name = ["app.test-mode"], havingValue = "false", matchIfMissing = true)
class OpenAIImageGenerationStrategy(
    @param:Value("\${OPENAI_API_KEY}") private val apiKey: String,
    private val promptQueryService: PromptQueryService,
) : ImageGenerationStrategy {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val OPENAI_API_URL = "https://api.openai.com/v1/images/edits"
        private const val REQUEST_TIMEOUT_MS = 300000L // 5 minutes in milliseconds
    }

    init {
        logger.info { "OpenAI Image Generation Strategy initialized - Real AI generation active" }
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
                requestTimeout = REQUEST_TIMEOUT_MS
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
            httpClient
                .post(OPENAI_API_URL) {
                    header(HttpHeaders.Authorization, "Bearer $apiKey")
                    setBody(MultiPartFormDataContent(formData))
                }

        if (!httpResponse.status.isSuccess()) {
            val errorBody = httpResponse.bodyAsText()
            logger.error { "OpenAI API returned error status ${httpResponse.status}: $errorBody" }
            throw IllegalStateException("OpenAI API error: ${httpResponse.status} - $errorBody")
        }

        val response = httpResponse.body<OpenAIResponse>()
        logger.info { "Successfully received response from OpenAI API" }

        validateOpenAIResponse(response)
        return response
    }

    private fun validateOpenAIResponse(response: OpenAIResponse) {
        if (response.error != null) {
            logger.error { "OpenAI API returned error: ${response.error.message}" }
            throw IllegalStateException("OpenAI API error: ${response.error.message}")
        }

        if (response.data.isNullOrEmpty()) {
            logger.error { "OpenAI API returned empty or null data" }
            throw IllegalStateException("OpenAI API returned no images")
        }
    }

    /**
     * Extracts image bytes from OpenAI response data.
     */
    private suspend fun extractImageBytes(responseData: List<OpenAIImage>): List<ByteArray> =
        responseData.map { openAIImage: OpenAIImage ->
            when {
                openAIImage.url != null -> {
                    // Download image from URL
                    logger.debug { "Downloading image from URL: ${openAIImage.url}" }
                    httpClient.get(openAIImage.url).body()
                }
                openAIImage.b64Json != null -> {
                    // Decode base64 image
                    logger.debug { "Decoding base64 image" }
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
            logger.info { "Starting OpenAI image generation with prompt ID: ${request.promptId}" }

            val prompt = promptQueryService.getPromptById(request.promptId)

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
                logger.info { "OpenAI API call was cancelled" }
                throw e // Re-throw cancellation exceptions
            } catch (e: HttpRequestTimeoutException) {
                logger.error(e) { "OpenAI API call timed out" }
                throw IllegalStateException("Failed to generate images: Request timed out", e)
            } catch (e: SocketTimeoutException) {
                logger.error(e) { "Socket timeout during OpenAI API call" }
                throw IllegalStateException("Failed to generate images: Socket timeout", e)
            } catch (e: ClientRequestException) {
                logger.error(e) { "Client error during OpenAI API call: ${e.response.status}" }
                throw IllegalStateException("Failed to generate images: Client error (${e.response.status})", e)
            } catch (e: ServerResponseException) {
                logger.error(e) { "Server error during OpenAI API call: ${e.response.status}" }
                throw IllegalStateException("Failed to generate images: Server error (${e.response.status})", e)
            } catch (e: ResponseException) {
                logger.error(e) { "HTTP response error during OpenAI API call" }
                throw IllegalStateException("Failed to generate images: HTTP error (${e.response.status})", e)
            } catch (e: JsonProcessingException) {
                logger.error(e) { "JSON processing error during OpenAI API call" }
                throw IllegalStateException("Failed to generate images: JSON processing error", e)
            } catch (e: IOException) {
                logger.error(e) { "IO error during OpenAI API call" }
                throw IllegalStateException("Failed to generate images: IO error", e)
            }
        }

    override fun testPrompt(
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
                        background = null, // testPrompt doesn't use background in API call
                        n = 1,
                        responseFormat = "url",
                    )

                val openAIImage = response.data!!.first()
                val imageUrl = openAIImage.url ?: throw IllegalStateException("No image URL returned")

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
                            quality = request.getQuality().name.lowercase(),
                            background = request.getBackground().name.lowercase(),
                        ),
                )
            } catch (e: CancellationException) {
                logger.info { "OpenAI API call was cancelled" }
                throw e // Re-throw cancellation exceptions
            } catch (e: HttpRequestTimeoutException) {
                logger.error(e) { "OpenAI API call timed out" }
                throw IllegalStateException("Failed to test prompt: Request timed out", e)
            } catch (e: SocketTimeoutException) {
                logger.error(e) { "Socket timeout during OpenAI API call" }
                throw IllegalStateException("Failed to test prompt: Socket timeout", e)
            } catch (e: ClientRequestException) {
                logger.error(e) { "Client error during OpenAI API call: ${e.response.status}" }
                throw IllegalStateException("Failed to test prompt: Client error (${e.response.status})", e)
            } catch (e: ServerResponseException) {
                logger.error(e) { "Server error during OpenAI API call: ${e.response.status}" }
                throw IllegalStateException("Failed to test prompt: Server error (${e.response.status})", e)
            } catch (e: ResponseException) {
                logger.error(e) { "HTTP response error during OpenAI API call" }
                throw IllegalStateException("Failed to test prompt: HTTP error (${e.response.status})", e)
            } catch (e: JsonProcessingException) {
                logger.error(e) { "JSON processing error during OpenAI API call" }
                throw IllegalStateException("Failed to test prompt: JSON processing error", e)
            } catch (e: IOException) {
                logger.error(e) { "IO error during OpenAI API call" }
                throw IllegalStateException("Failed to test prompt: IO error", e)
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
