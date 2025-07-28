package com.jotoai.voenix.shop.domain.images.service

import com.jotoai.voenix.shop.common.exception.BadRequestException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.images.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.domain.images.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.domain.openai.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.domain.openai.service.OpenAIImageService
import com.jotoai.voenix.shop.domain.prompts.service.PromptService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional(readOnly = true)
class PublicImageGenerationService(
    private val openAIImageService: OpenAIImageService,
    private val promptService: PromptService,
    private val imageService: ImageService,
    @Value("\${app.base-url:http://localhost:8080}") private val baseUrl: String,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PublicImageGenerationService::class.java)
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB
        private val ALLOWED_CONTENT_TYPES = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")
    }

    @Transactional
    fun generateImage(
        imageFile: MultipartFile,
        request: PublicImageGenerationRequest,
        httpRequest: HttpServletRequest,
    ): PublicImageGenerationResponse {
        // Validate file
        validateImageFile(imageFile)

        // Validate prompt exists and is active
        val prompt = promptService.getPromptById(request.promptId)
        if (!prompt.active) {
            throw BadRequestException("The selected prompt is not available")
        }

        logger.info("Processing public image generation request for prompt ID: ${request.promptId}")

        try {
            // Create OpenAI request with fixed parameters for public use
            val openAIRequest =
                CreateImageEditRequest(
                    promptId = request.promptId,
                    background = request.background,
                    quality = request.quality,
                    size = request.size,
                    n = 1, // Always generate 1 image for public users
                )

            // Generate image using existing OpenAI service
            val imageEditResponse = openAIImageService.editImage(imageFile, openAIRequest)

            // Convert filenames to full URLs
            val imageUrls =
                imageEditResponse.imageFilenames.map { filename ->
                    "$baseUrl/api/public/images/$filename"
                }

            logger.info("Successfully generated ${imageUrls.size} images for public user")

            return PublicImageGenerationResponse(
                imageUrls = imageUrls,
            )
        } catch (e: Exception) {
            logger.error("Error generating image for public user", e)
            when (e) {
                is BadRequestException -> throw e
                is ResourceNotFoundException -> throw e
                else -> throw RuntimeException("Failed to generate image. Please try again later.")
            }
        }
    }

    private fun validateImageFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw BadRequestException("Image file is required")
        }

        if (file.size > MAX_FILE_SIZE) {
            throw BadRequestException("Image file size must be less than 10MB")
        }

        val contentType = file.contentType?.lowercase() ?: ""
        if (contentType !in ALLOWED_CONTENT_TYPES) {
            throw BadRequestException("Invalid image format. Allowed formats: JPEG, PNG, WebP")
        }
    }
}
