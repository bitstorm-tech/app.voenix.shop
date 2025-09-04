package com.jotoai.voenix.shop.openai.internal.provider

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.jotoai.voenix.shop.image.ImageData
import com.jotoai.voenix.shop.image.ImageMetadata
import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.image.ImageType
import com.jotoai.voenix.shop.openai.internal.dto.ImageEditBytesResponse
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptRequest
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptRequestParams
import com.jotoai.voenix.shop.openai.internal.dto.TestPromptResponse
import com.jotoai.voenix.shop.openai.internal.exception.ImageGenerationException
import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.UUID
import kotlin.io.encoding.Base64

@Component
internal class GeminiImageProvider(
    @Value("\${GOOGLE_API_KEY:}") private val googleApiKey: String,
    private val httpClient: HttpClient,
    private val imageService: ImageService,
) : ImageGenerationProvider {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/" +
                "gemini-2.5-flash-image-preview:generateContent"
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GeminiRequest(
        val contents: List<GeminiContent>,
        val generationConfig: GeminiGenerationConfig? = null,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GeminiContent(
        val parts: List<GeminiPart>,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GeminiPart(
        val text: String? = null,
        val inlineData: GeminiInlineData? = null,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GeminiInlineData(
        val mimeType: String,
        val data: String, // base64 encoded
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GeminiGenerationConfig(
        val candidateCount: Int = 1,
        val maxOutputTokens: Int? = null,
        val temperature: Float? = null,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GeminiResponse(
        val candidates: List<GeminiCandidate>? = null,
        val error: GeminiError? = null,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GeminiCandidate(
        val content: GeminiContent? = null,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class GeminiError(
        val code: Int,
        val message: String,
        val status: String? = null,
    )

    override suspend fun generateImages(
        imageFile: MultipartFile,
        prompt: PromptDto,
        options: GenerationOptions,
    ): ImageEditBytesResponse {
        logger.info { "Starting Google Gemini image generation with prompt ID: ${prompt.id}" }

        if (googleApiKey.isEmpty()) {
            throw ImageGenerationException("Google API key not configured")
        }

        return executeApiCall("Google image generation") {
            val promptText = buildFinalPrompt(prompt)
            val response =
                callGeminiAPI(
                    imageFile = imageFile,
                    promptText = promptText,
                    n = options.n,
                )

            logger.info { "Response: $response" }

            val imageBytesList = extractGeminiImageBytes(response.candidates!!)
            ImageEditBytesResponse(imageBytes = imageBytesList)
        }
    }

    override suspend fun testPrompt(
        imageFile: MultipartFile,
        request: TestPromptRequest,
    ): TestPromptResponse {
        logger.info { "Starting Google Gemini prompt test with master prompt: ${request.masterPrompt}" }

        if (googleApiKey.isEmpty()) {
            throw ImageGenerationException("Google API key not configured")
        }

        return executeApiCall("Google prompt testing") {
            val combinedPrompt = "${request.masterPrompt} ${request.specificPrompt}".trim()

            val response =
                callGeminiAPI(
                    imageFile = imageFile,
                    promptText = combinedPrompt,
                    n = 1,
                )
            logger.info { "Response: $response" }

            val imageBytes = extractGeminiImageBytes(response.candidates!!)
            val imageData = ImageData.Bytes(imageBytes.first(), "gemini_test_${UUID.randomUUID()}.png")
            val metadata = ImageMetadata(type = ImageType.PRIVATE)
            val storedImage = imageService.store(imageData, metadata)
            val imageUrl = imageService.getUrl(storedImage.filename, ImageType.PRIVATE)

            TestPromptResponse(
                imageUrl = imageUrl,
                requestParams =
                    TestPromptRequestParams(
                        model = "gemini-2.5-flash-image-preview",
                        size = request.getSize().apiValue,
                        n = 1,
                        responseFormat = "base64",
                        masterPrompt = request.masterPrompt,
                        specificPrompt = request.specificPrompt,
                        combinedPrompt = combinedPrompt,
                        quality = request.getQuality().apiValue,
                        background = request.getBackground().apiValue,
                    ),
            )
        }
    }

    private suspend fun callGeminiAPI(
        imageFile: MultipartFile,
        promptText: String,
        n: Int = 1,
    ): GeminiResponse {
        val imageBytes = imageFile.bytes
        val base64Image = Base64.encode(imageBytes)
        val mimeType = getContentType(imageFile.originalFilename ?: "image.png")

        val requestBody =
            GeminiRequest(
                contents =
                    listOf(
                        GeminiContent(
                            parts =
                                listOf(
                                    GeminiPart(text = promptText),
                                    GeminiPart(
                                        inlineData =
                                            GeminiInlineData(
                                                mimeType = mimeType,
                                                data = base64Image,
                                            ),
                                    ),
                                ),
                        ),
                    ),
                generationConfig =
                    GeminiGenerationConfig(
                        candidateCount = n,
                        maxOutputTokens = 8192,
                        temperature = 0.7f,
                    ),
            )

        val httpResponse =
            httpClient.post("$GEMINI_API_URL?key=$googleApiKey") {
                header("Content-Type", "application/json")
                setBody(requestBody)
            }

        if (!httpResponse.status.isSuccess()) {
            val errorBody = httpResponse.bodyAsText()
            logger.error { "Google Gemini API returned error status ${httpResponse.status}: $errorBody" }
            error("Google Gemini API error: ${httpResponse.status} - $errorBody")
        }

        val response = httpResponse.body<GeminiResponse>()
        logger.info { "Successfully received response from Google Gemini API" }

        validateGeminiResponse(response)
        return response
    }

    private fun extractGeminiImageBytes(candidates: List<GeminiCandidate>): List<ByteArray> {
        val images = candidates.flatMap { extractImagesFromCandidate(it) }
        if (images.isEmpty()) {
            error("Google Gemini response contains no image data")
        }
        return images
    }

    private fun extractImagesFromCandidate(candidate: GeminiCandidate): List<ByteArray> {
        val parts = candidate.content?.parts ?: return emptyList()
        return parts.mapNotNull { extractImageFromPart(it) }
    }

    private fun extractImageFromPart(part: GeminiPart): ByteArray? {
        val inlineData = part.inlineData ?: return null
        logger.debug { "Decoding image: ${inlineData.mimeType}" }
        return Base64.decode(inlineData.data)
    }

    private fun validateGeminiResponse(response: GeminiResponse) {
        if (response.error != null) {
            logger.error { "Google Gemini API returned error: ${response.error.message}" }
            error("Google Gemini API error: ${response.error.message}")
        }

        if (response.candidates.isNullOrEmpty()) {
            logger.error { "Google Gemini API returned empty or null candidates" }
            error("Google Gemini API returned no candidates")
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
