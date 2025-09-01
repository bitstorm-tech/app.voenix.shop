package com.jotoai.voenix.shop.openai.internal.service

import com.jotoai.voenix.shop.image.ImageData
import com.jotoai.voenix.shop.image.ImageMetadata
import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.image.ImageType
import com.jotoai.voenix.shop.openai.CreateImageEditRequest
import com.jotoai.voenix.shop.openai.ImageEditBytesResponse
import com.jotoai.voenix.shop.openai.ImageEditResponse
import com.jotoai.voenix.shop.openai.TestPromptRequest
import com.jotoai.voenix.shop.openai.TestPromptResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

/**
 * Simplified OpenAI image facade that delegates to the unified OpenAI service.
 */
@Service
class OpenAIImageFacadeImpl(
    private val imageService: ImageService,
    private val openAIImageService: OpenAIImageService,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Edits an image and returns raw image bytes without storing them.
     */
    fun editImageBytes(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
    ): ImageEditBytesResponse {
        logger.info { "Starting image edit request (bytes mode) with prompt ID: ${request.promptId}" }
        return openAIImageService.generateImages(imageFile, request)
    }

    /**
     * Edits an image and stores the results, returning filenames.
     */
    fun editImage(
        imageFile: MultipartFile,
        request: CreateImageEditRequest,
    ): ImageEditResponse {
        logger.info { "Starting image edit request with prompt ID: ${request.promptId}" }

        // Generate images using the unified service
        val bytesResponse = openAIImageService.generateImages(imageFile, request)

        // Store images and return URLs
        val savedImageFilenames =
            bytesResponse.imageBytes.map { imageBytes ->
                val imageData = ImageData.Bytes(imageBytes, "generated-image.png")
                val metadata = ImageMetadata(type = ImageType.PRIVATE)
                val storedImage = imageService.store(imageData, metadata)
                storedImage.filename
            }

        return ImageEditResponse(imageFilenames = savedImageFilenames)
    }

    /**
     * Tests a prompt with the unified service.
     */
    fun testPrompt(
        imageFile: MultipartFile,
        request: TestPromptRequest,
    ): TestPromptResponse {
        logger.info { "Starting prompt test with master prompt: ${request.masterPrompt}" }
        return openAIImageService.testPrompt(imageFile, request)
    }
}
