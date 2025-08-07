package com.jotoai.voenix.shop.domain.openai.service

import com.jotoai.voenix.shop.domain.openai.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.domain.openai.dto.ImageEditBytesResponse
import com.jotoai.voenix.shop.domain.openai.dto.ImageEditResponse
import com.jotoai.voenix.shop.domain.openai.dto.TestPromptRequest
import com.jotoai.voenix.shop.domain.openai.dto.TestPromptResponse
import com.jotoai.voenix.shop.image.api.ImageStorageService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.InputStream

/**
 * Service that manages image generation using different strategies.
 * Delegates actual image generation to the configured ImageGenerationStrategy.
 */
@Service
class OpenAIImageService(
    private val imageStorageService: ImageStorageService,
    private val imageGenerationStrategy: ImageGenerationStrategy,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(OpenAIImageService::class.java)
    }

    /**
     * Edits an image and returns raw image bytes without storing them.
     * Delegates to the configured ImageGenerationStrategy.
     */
    fun editImageBytes(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
    ): ImageEditBytesResponse {
        logger.info("Starting image edit request (bytes mode) with prompt ID: ${request.promptId}")
        return imageGenerationStrategy.generateImages(imageFile, request)
    }

    /**
     * Edits an image and stores the results, returning filenames.
     * Uses the strategy pattern for generation and then handles storage.
     */
    fun editImage(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
    ): ImageEditResponse {
        logger.info("Starting image edit request with prompt ID: ${request.promptId}")

        try {
            // Generate images using the configured strategy
            val bytesResponse = imageGenerationStrategy.generateImages(imageFile, request)

            // Store images and return URLs
            val savedImageFilenames =
                bytesResponse.imageBytes.map { imageBytes ->
                    // ImageService will handle UUID generation
                    val multipartFile =
                        SimpleMultipartFile(
                            "generated-image.png",
                            "image/png",
                            imageBytes,
                        )
                    imageStorageService.storeFile(multipartFile, ImageType.PRIVATE)
                }

            return ImageEditResponse(imageFilenames = savedImageFilenames)
        } catch (e: Exception) {
            logger.error("Error during image generation and storage", e)
            throw RuntimeException("Failed to edit image: ${e.message}", e)
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

    /**
     * Tests a prompt with the configured generation strategy.
     */
    fun testPrompt(
        imageFile: MultipartFile,
        request: TestPromptRequest,
    ): TestPromptResponse {
        logger.info("Starting prompt test with master prompt: ${request.masterPrompt}")
        return imageGenerationStrategy.testPrompt(imageFile, request)
    }
}
