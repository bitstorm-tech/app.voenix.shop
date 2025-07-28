package com.jotoai.voenix.shop.domain.images.service

import com.jotoai.voenix.shop.common.exception.BadRequestException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.images.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.domain.images.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.domain.openai.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.domain.openai.service.OpenAIImageService
import com.jotoai.voenix.shop.domain.prompts.service.PromptService
import com.jotoai.voenix.shop.domain.ratelimit.service.RateLimitService
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
    private val rateLimitService: RateLimitService,
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

        // Generate identifier for rate limiting (IP + session combination)
        val sessionToken =
            httpRequest.session.getAttribute("sessionToken") as? String
                ?: rateLimitService.generateSessionToken().also {
                    httpRequest.session.setAttribute("sessionToken", it)
                }

        val clientIp = getClientIp(httpRequest)
        val rateLimitIdentifier = "$clientIp:$sessionToken"

        // Check rate limit
        if (!rateLimitService.checkRateLimit(rateLimitIdentifier)) {
            val remaining = rateLimitService.getRemainingAttempts(rateLimitIdentifier)
            throw BadRequestException("Rate limit exceeded. You have $remaining image generations remaining this hour.")
        }

        logger.info("Processing public image generation request for prompt ID: ${request.promptId}, IP: $clientIp")

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
                sessionToken = sessionToken,
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

    private fun getClientIp(request: HttpServletRequest): String {
        // Check for forwarded IP (when behind proxy/load balancer)
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",")[0].trim()
        }

        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp
        }

        return request.remoteAddr
    }
}
