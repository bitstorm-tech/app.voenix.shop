package com.jotoai.voenix.shop.openai.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.jotoai.voenix.shop.openai.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.openai.dto.GeneratedImage
import com.jotoai.voenix.shop.openai.dto.ImageEditResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.util.UUID

@Service
class OpenAIImageService(
    @Value("\${OPENAI_API_KEY}") private val apiKey: String,
    private val objectMapper: ObjectMapper,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(OpenAIImageService::class.java)
        private const val OPENAI_API_URL = "https://api.openai.com/v1/images/edits"
        private const val TIMEOUT_MS = 30000
    }

    private val boundary = "----WebKitFormBoundary${UUID.randomUUID().toString().replace("-", "")}"

    fun editImage(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
    ): ImageEditResponse {
        logger.info("Starting image edit request with prompt: ${request.prompt}")

        val connection = createConnection()

        try {
            DataOutputStream(connection.outputStream).use { outputStream ->
                // Write image file
                writeFilePart(outputStream, "image", imageFile.originalFilename ?: "image.png", imageFile.bytes)

                // Write prompt
                writeFormField(outputStream, "prompt", request.prompt)

                // Write other parameters
                writeFormField(outputStream, "n", request.n.toString())
                writeFormField(outputStream, "size", request.size.apiValue)
                writeFormField(outputStream, "response_format", "url")

                // Write background parameter if not AUTO
                if (request.background != com.jotoai.voenix.shop.openai.dto.enums.ImageBackground.AUTO) {
                    writeFormField(outputStream, "transparency", request.background.apiValue)
                }

                // Map quality to model parameter
                val model =
                    when (request.quality) {
                        com.jotoai.voenix.shop.openai.dto.enums.ImageQuality.LOW -> "dall-e-2"
                        com.jotoai.voenix.shop.openai.dto.enums.ImageQuality.MEDIUM -> "dall-e-2"
                        com.jotoai.voenix.shop.openai.dto.enums.ImageQuality.HIGH -> "dall-e-3"
                    }
                writeFormField(outputStream, "model", model)

                // Write closing boundary
                outputStream.writeBytes("--$boundary--\r\n")
                outputStream.flush()
            }

            return handleResponse(connection)
        } catch (e: Exception) {
            logger.error("Error during OpenAI API call", e)
            throw RuntimeException("Failed to edit image: ${e.message}", e)
        } finally {
            connection.disconnect()
        }
    }

    private fun createConnection(): HttpURLConnection {
        val url = URI(OPENAI_API_URL).toURL()
        val connection = url.openConnection() as HttpURLConnection

        connection.apply {
            requestMethod = "POST"
            doOutput = true
            doInput = true
            connectTimeout = TIMEOUT_MS
            readTimeout = TIMEOUT_MS
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        }

        return connection
    }

    private fun writeFilePart(
        outputStream: DataOutputStream,
        fieldName: String,
        fileName: String,
        fileData: ByteArray,
    ) {
        outputStream.writeBytes("--$boundary\r\n")
        outputStream.writeBytes("Content-Disposition: form-data; name=\"$fieldName\"; filename=\"$fileName\"\r\n")
        outputStream.writeBytes("Content-Type: ${getContentType(fileName)}\r\n")
        outputStream.writeBytes("Content-Transfer-Encoding: binary\r\n")
        outputStream.writeBytes("\r\n")
        outputStream.write(fileData)
        outputStream.writeBytes("\r\n")
    }

    private fun writeFormField(
        outputStream: DataOutputStream,
        fieldName: String,
        value: String,
    ) {
        outputStream.writeBytes("--$boundary\r\n")
        outputStream.writeBytes("Content-Disposition: form-data; name=\"$fieldName\"\r\n")
        outputStream.writeBytes("\r\n")
        outputStream.writeBytes("$value\r\n")
    }

    private fun getContentType(fileName: String): String =
        when {
            fileName.endsWith(".png", ignoreCase = true) -> "image/png"
            fileName.endsWith(".jpg", ignoreCase = true) ||
                fileName.endsWith(".jpeg", ignoreCase = true) -> "image/jpeg"
            fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
            else -> "application/octet-stream"
        }

    private fun handleResponse(connection: HttpURLConnection): ImageEditResponse {
        val responseCode = connection.responseCode
        logger.info("OpenAI API response code: $responseCode")

        val responseBody =
            if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error details"
            }

        logger.debug("OpenAI API response body: $responseBody")

        if (responseCode !in 200..299) {
            logger.error("OpenAI API error response: $responseBody")
            throw RuntimeException("OpenAI API error (HTTP $responseCode): $responseBody")
        }

        return parseResponse(responseBody)
    }

    private fun parseResponse(responseBody: String): ImageEditResponse {
        try {
            val jsonNode = objectMapper.readTree(responseBody)
            val dataArray = jsonNode.get("data")

            val images = mutableListOf<GeneratedImage>()
            dataArray?.forEach { imageNode ->
                images.add(
                    GeneratedImage(
                        url = imageNode.get("url")?.asText(),
                        b64Json = imageNode.get("b64_json")?.asText(),
                        revisedPrompt = imageNode.get("revised_prompt")?.asText(),
                    ),
                )
            }

            return ImageEditResponse(images)
        } catch (e: Exception) {
            logger.error("Failed to parse OpenAI response", e)
            throw RuntimeException("Failed to parse OpenAI response: ${e.message}", e)
        }
    }
}
