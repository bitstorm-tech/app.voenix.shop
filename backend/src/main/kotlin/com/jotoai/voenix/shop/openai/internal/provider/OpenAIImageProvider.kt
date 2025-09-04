package com.jotoai.voenix.shop.openai.internal.provider

import com.fasterxml.jackson.annotation.JsonProperty
import com.jotoai.voenix.shop.image.ImageData
import com.jotoai.voenix.shop.image.ImageMetadata
import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.image.ImageType
import com.jotoai.voenix.shop.openai.internal.dto.ImageEditBytesResponse
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptRequest
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptRequestParams
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptResponse
import com.jotoai.voenix.shop.openai.internal.exception.ImageGenerationException
import com.jotoai.voenix.shop.prompt.PromptDto
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
import kotlinx.coroutines.CancellationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import kotlin.io.encoding.Base64

@Component
internal class OpenAIImageProvider(
    @Value($$"${OPENAI_API_KEY:}") private val apiKey: String,
    private val httpClient: HttpClient,
    private val imageService: ImageService,
) : ImageGenerationProvider {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val OPENAI_API_URL = "https://api.openai.com/v1/images/edits"
    }

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

    override suspend fun generateImages(
        imageFile: MultipartFile,
        prompt: PromptDto,
        options: GenerationOptions,
    ): ImageEditBytesResponse {
        logger.info { "Starting OpenAI image generation with prompt ID: ${prompt.id}" }

        return executeApiCall("OpenAI image generation") {
            val response =
                callOpenAIEditAPI(
                    imageFile = imageFile,
                    promptText = buildFinalPrompt(prompt),
                    size = options.size,
                    background = options.background,
                    n = options.n,
                )

            val imageBytesList = extractImageBytes(response.data!!)
            ImageEditBytesResponse(imageBytes = imageBytesList)
        }
    }

    override suspend fun testPrompt(
        imageFile: MultipartFile,
        request: TestPromptRequest,
    ): TestPromptResponse {
        logger.info { "Starting OpenAI prompt test with master prompt: ${request.masterPrompt}" }

        return executeApiCall("OpenAI prompt testing") {
            val combinedPrompt = "${request.masterPrompt} ${request.specificPrompt}".trim()

            val response =
                callOpenAIEditAPI(
                    imageFile = imageFile,
                    promptText = combinedPrompt,
                    size = request.getSize().apiValue,
                    background = null,
                    n = 1,
                )

            val openAIImage = response.data!!.first()
            val imageUrl =
                when {
                    openAIImage.url != null -> openAIImage.url
                    openAIImage.b64Json != null -> {
                        val bytes = Base64.decode(openAIImage.b64Json)
                        val stored =
                            imageService.store(
                                ImageData.Bytes(bytes, "openai_test_${System.currentTimeMillis()}.png"),
                                ImageMetadata(type = ImageType.PROMPT_TEST),
                            )
                        imageService.getUrl(stored.filename, ImageType.PROMPT_TEST)
                    }
                    else -> error("OpenAI response contains neither URL nor base64 data")
                }

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
        }
    }

    private suspend fun callOpenAIEditAPI(
        imageFile: MultipartFile,
        promptText: String,
        size: String,
        background: String? = null,
        n: Int = 1,
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

    private fun buildFinalPrompt(prompt: PromptDto): String {
        val mainPrompt = listOfNotNull(prompt.promptText)
        val slotPrompts =
            prompt.slots
                .sortedBy { it.promptSlotType?.position ?: 0 }
                .mapNotNull { it.prompt }

        return (mainPrompt + slotPrompts)
            .joinToString(" ")
            .trim()
    }

    private fun getContentType(fileName: String): String =
        when {
            fileName.endsWith(".png", ignoreCase = true) -> "image/png"
            fileName.endsWith(".jpg", ignoreCase = true) ||
                fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
            else -> "application/octet-stream"
        }

    private inline fun <T> executeApiCall(
        operation: String,
        block: () -> T,
    ): T =
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "$operation failed" }
            throw ImageGenerationException("$operation failed: ${e.message}", e)
        }
}
