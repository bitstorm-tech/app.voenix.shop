package com.jotoai.voenix.shop.domain.openai.service

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.jotoai.voenix.shop.domain.images.dto.CreateImageRequest
import com.jotoai.voenix.shop.domain.images.dto.ImageType
import com.jotoai.voenix.shop.domain.images.service.ImageService
import com.jotoai.voenix.shop.domain.openai.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.domain.openai.dto.ImageEditResponse
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
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.io.encoding.Base64
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class OpenAIImageService(
    @param:Value($$"${OPENAI_API_KEY}") private val apiKey: String,
    private val imageService: ImageService,
    private val promptService: PromptService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(OpenAIImageService::class.java)
        private const val OPENAI_API_URL = "https://api.openai.com/v1/images/edits"
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
                requestTimeout = 120000
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

    fun editImage(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
    ): ImageEditResponse =
        runBlocking {
            logger.info("Starting image edit request with prompt ID: ${request.promptId}")

            val prompt = promptService.getPromptById(request.promptId)

            try {
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

                        append("prompt", buildFinalPrompt(prompt))
                        append("n", request.n.toString())
                        append("size", request.size.apiValue)
                        append("background", request.background.apiValue)
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

                // Check for error response
                if (response.error != null) {
                    logger.error("OpenAI API returned error: ${response.error.message}")
                    throw RuntimeException("OpenAI API error: ${response.error.message}")
                }

                // Check if data is null or empty
                if (response.data.isNullOrEmpty()) {
                    logger.error("OpenAI API returned empty or null data")
                    throw RuntimeException("OpenAI API returned no images")
                }

                // Download and save images, then return URLs
                val savedImageFilenames =
                    response.data.map { openAIImage: OpenAIImage ->
                        val imageBytes =
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

                        // Create a MultipartFile from the bytes
                        // ImageService will handle UUID generation
                        val multipartFile =
                            SimpleMultipartFile(
                                "generated-image.png",
                                "image/png",
                                imageBytes,
                            )

                        // Save the image using ImageService
                        val savedImage =
                            imageService.store(
                                multipartFile,
                                CreateImageRequest(imageType = ImageType.PRIVATE),
                            )

                        savedImage.filename
                    }

                ImageEditResponse(imageFilenames = savedImageFilenames)
            } catch (e: Exception) {
                logger.error("Error during OpenAI API call", e)
                throw RuntimeException("Failed to edit image: ${e.message}", e)
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

    private class SimpleMultipartFile(
        private val fileName: String,
        private val contentType: String,
        private val content: ByteArray,
    ) : MultipartFile {
        override fun getName(): String = "file"

        override fun getOriginalFilename(): String = fileName

        override fun getContentType(): String = contentType

        override fun isEmpty(): Boolean = content.isEmpty()

        override fun getSize(): Long = content.size.toLong()

        override fun getBytes(): ByteArray = content

        override fun getInputStream(): InputStream = ByteArrayInputStream(content)

        override fun transferTo(dest: java.io.File) {
            dest.writeBytes(content)
        }
    }

    private fun buildFinalPrompt(prompt: PromptDto): String {
        val parts = mutableListOf<String>()

        // Add base prompt content if exists
        prompt.promptText?.let { parts.add(it) }

        // Add slot prompts sorted by position
        prompt.slots
            .sortedBy { it.slotType?.position ?: 0 }
            .forEach { slot ->
                slot.prompt?.let { parts.add(it) }
            }

        return parts.joinToString(" ")
    }
}
