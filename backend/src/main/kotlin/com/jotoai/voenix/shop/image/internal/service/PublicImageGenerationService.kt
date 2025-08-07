package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.common.exception.BadRequestException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.image.internal.domain.GeneratedImage
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.domain.openai.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.domain.openai.service.OpenAIImageService
import com.jotoai.voenix.shop.domain.prompts.service.PromptService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class PublicImageGenerationService(
    private val openAIImageService: OpenAIImageService,
    private val promptService: PromptService,
    private val generatedImageRepository: GeneratedImageRepository,
    private val request: HttpServletRequest,
    private val storagePathService: StoragePathService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PublicImageGenerationService::class.java)
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB
        private val ALLOWED_CONTENT_TYPES = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")
        private const val RATE_LIMIT_HOURS = 1
        private const val MAX_GENERATIONS_PER_IP_PER_HOUR = 10
    }

    @Transactional
    fun generateImage(
        imageFile: MultipartFile,
        request: PublicImageGenerationRequest,
    ): PublicImageGenerationResponse {
        validateImageFile(imageFile)

        val ipAddress = getClientIpAddress()

        checkIpRateLimit(ipAddress)

        val prompt = promptService.getPromptById(request.promptId)
        if (!prompt.active) {
            throw BadRequestException("The selected prompt is not available")
        }

        logger.info("Processing public image generation request for prompt ID: ${request.promptId}")

        try {
            val openAIRequest =
                CreateImageEditRequest(
                    promptId = request.promptId,
                    background = request.background,
                    quality = request.quality,
                    size = request.size,
                    n = request.n,
                )

            logger.debug("Generated OpenAI request: {}", openAIRequest)

            // Generate image using existing OpenAI service
            val imageEditResponse = openAIImageService.editImage(imageFile, openAIRequest)

            // Store image generation records for tracking (anonymous user)
            val generatedImages =
                imageEditResponse.imageFilenames.map { filename ->
                    val generatedImage =
                        GeneratedImage(
                            filename = filename,
                            promptId = request.promptId,
                            userId = null, // Anonymous user
                            ipAddress = ipAddress,
                            generatedAt = LocalDateTime.now(),
                        )
                    generatedImageRepository.save(generatedImage)
                }

            // Convert filenames to full URLs using StoragePathService
            val imageUrls =
                imageEditResponse.imageFilenames.map { filename ->
                    storagePathService.getImageUrl(ImageType.PUBLIC, filename)
                }

            val imageIds = generatedImages.mapNotNull { it.id }

            logger.info("Successfully generated ${imageUrls.size} images for public user")

            return PublicImageGenerationResponse(
                imageUrls = imageUrls,
                generatedImageIds = imageIds,
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

    private fun checkIpRateLimit(ipAddress: String) {
        val startTime = LocalDateTime.now().minusHours(RATE_LIMIT_HOURS.toLong())
        val generationCount = generatedImageRepository.countByIpAddressAndGeneratedAtAfter(ipAddress, startTime)

        if (generationCount >= MAX_GENERATIONS_PER_IP_PER_HOUR) {
            throw BadRequestException(
                "Rate limit exceeded. You can generate up to $MAX_GENERATIONS_PER_IP_PER_HOUR images per hour. Please try again later.",
            )
        }

        logger.debug("IP $ipAddress has generated $generationCount images in the last $RATE_LIMIT_HOURS hour(s)")
    }

    private fun getClientIpAddress(): String {
        // Check for forwarded IP addresses (when behind a proxy/load balancer)
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",")[0].trim()
        }

        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp
        }

        // Fall back to remote address
        return request.remoteAddr
    }
}
