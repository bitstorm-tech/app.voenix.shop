package com.jotoai.voenix.shop.openai.service

import com.jotoai.voenix.shop.openai.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.openai.dto.GeneratedImage
import com.jotoai.voenix.shop.openai.dto.ImageEditResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.jackson.jackson
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class OpenAIImageService(
    @Value("\${OPENAI_API_KEY}") private val apiKey: String,
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
                requestTimeout = 60000
            }
        }

    data class OpenAIResponse(
        val data: List<OpenAIImage>,
    )

    data class OpenAIImage(
        val url: String? = null,
        val b64_json: String? = null,
        val revised_prompt: String? = null,
    )

    fun editImage(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
    ): ImageEditResponse =
        runBlocking {
            logger.info("Starting image edit request with prompt: ${request.prompt}")

            try {
                val formData =
                    formData {
                        // Add image file
                        append(
                            "image",
                            imageFile.bytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, getContentType(imageFile.originalFilename ?: "image.png"))
                                append(HttpHeaders.ContentDisposition, "filename=\"${imageFile.originalFilename ?: "image.png"}\"")
                            },
                        )

                        // Add form fields
                        append("prompt", request.prompt)
                        append("n", request.n.toString())
                        append("size", request.size.apiValue)
                        append("response_format", "url")
                        append("transparency", request.background.apiValue)
                    }

                val response =
                    httpClient
                        .post(OPENAI_API_URL) {
                            header(HttpHeaders.Authorization, "Bearer $apiKey")
                            setBody(MultiPartFormDataContent(formData))
                        }.body<OpenAIResponse>()

                logger.info("Successfully received response from OpenAI API")

                // Convert OpenAI response to our DTO
                ImageEditResponse(
                    images =
                        response.data.map { image ->
                            GeneratedImage(
                                url = image.url,
                                b64Json = image.b64_json,
                                revisedPrompt = image.revised_prompt,
                            )
                        },
                )
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
}
