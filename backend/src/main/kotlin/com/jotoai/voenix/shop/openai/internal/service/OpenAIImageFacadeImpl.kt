package com.jotoai.voenix.shop.openai.internal.service

import com.jotoai.voenix.shop.image.ImageData
import com.jotoai.voenix.shop.image.ImageMetadata
import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.openai.ImageGenerationStrategy
import com.jotoai.voenix.shop.openai.CreateImageEditRequest
import com.jotoai.voenix.shop.openai.ImageEditBytesResponse
import com.jotoai.voenix.shop.openai.ImageEditResponse
import com.jotoai.voenix.shop.openai.TestPromptRequest
import com.jotoai.voenix.shop.openai.TestPromptResponse
import com.jotoai.voenix.shop.openai.ImageGenerationException
import com.jotoai.voenix.shop.image.ImageType
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CancellationException
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Implementation of OpenAI image facade that manages image generation using different strategies.
 * Delegates actual image generation to the configured ImageGenerationStrategy.
 */
@Service
class OpenAIImageFacadeImpl(
    private val imageService: ImageService,
    private val imageGenerationStrategy: ImageGenerationStrategy,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Edits an image and returns raw image bytes without storing them.
     * Delegates to the configured ImageGenerationStrategy.
     */
    fun editImageBytes(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
    ): ImageEditBytesResponse {
        logger.info { "Starting image edit request (bytes mode) with prompt ID: ${request.promptId}" }
        try {
            return imageGenerationStrategy.generateImages(imageFile, request)
        } catch (e: CancellationException) {
            logger.info { "Image generation was cancelled" }
            throw e // Re-throw cancellation exceptions
        } catch (e: IOException) {
            logger.error(e) { "IO error during image generation (bytes mode)" }
            throw ImageGenerationException("Failed to generate image bytes due to IO error: ${e.message}", e)
        }
    }

    fun editImageBytes(
        imageBytes: ByteArray,
        request: CreateImageEditRequest,
    ): ImageEditBytesResponse {
        val multipartFile = SimpleMultipartFile("image.png", "image/png", imageBytes)
        return editImageBytes(multipartFile, request)
    }

    /**
     * Edits an image and stores the results, returning filenames.
     * Uses the strategy pattern for generation and then handles storage.
     */
    fun editImage(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
    ): ImageEditResponse {
        logger.info { "Starting image edit request with prompt ID: ${request.promptId}" }

        try {
            // Generate images using the configured strategy
            val bytesResponse = imageGenerationStrategy.generateImages(imageFile, request)

            // Store images and return URLs
            val savedImageFilenames =
                bytesResponse.imageBytes.map { imageBytes ->
                    val imageData = ImageData.Bytes(imageBytes, "generated-image.png")
                    val metadata = ImageMetadata(type = ImageType.PRIVATE)
                    val storedImage = imageService.store(imageData, metadata)
                    storedImage.filename
                }

            return ImageEditResponse(imageFilenames = savedImageFilenames)
        } catch (e: CancellationException) {
            logger.info { "Image generation and storage was cancelled" }
            throw e // Re-throw cancellation exceptions
        } catch (e: IOException) {
            logger.error(e) { "IO error during image generation and storage" }
            throw ImageGenerationException("Failed to edit image due to IO error: ${e.message}", e)
        }
    }

    /**
     * Tests a prompt with the configured generation strategy.
     */
    fun testPrompt(
        imageFile: MultipartFile,
        request: TestPromptRequest,
    ): TestPromptResponse {
        logger.info { "Starting prompt test with master prompt: ${request.masterPrompt}" }
        try {
            return imageGenerationStrategy.testPrompt(imageFile, request)
        } catch (e: CancellationException) {
            logger.info { "Prompt testing was cancelled" }
            throw e // Re-throw cancellation exceptions
        } catch (e: IOException) {
            logger.error(e) { "IO error during prompt testing" }
            throw ImageGenerationException("Failed to test prompt due to IO error: ${e.message}", e)
        }
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
}
