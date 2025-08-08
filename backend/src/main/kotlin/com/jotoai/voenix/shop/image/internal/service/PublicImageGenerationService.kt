package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.domain.openai.service.OpenAIImageService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.image.internal.domain.GeneratedImage
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional(readOnly = true)
class PublicImageGenerationService(
    openAIImageService: OpenAIImageService,
    promptQueryService: PromptQueryService,
    generatedImageRepository: GeneratedImageRepository,
    private val request: HttpServletRequest,
    private val storagePathService: com.jotoai.voenix.shop.image.api.StoragePathService,
    private val imageStorageService: ImageStorageService,
) : BaseImageGenerationService(openAIImageService, promptQueryService, generatedImageRepository) {
    companion object {
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
        checkRateLimit(ipAddress)
        validatePrompt(request.promptId)

        logger.info("Processing public image generation request for prompt ID: ${request.promptId}")

        return executeWithErrorHandling(
            operation = { processImageGeneration(imageFile, request, ipAddress) },
            contextMessage = "generating image for public user",
        )
    }

    override fun checkRateLimit(identifier: String) {
        checkTimeBasedRateLimit(
            identifier = "IP $identifier",
            rateLimitHours = RATE_LIMIT_HOURS,
            maxGenerations = MAX_GENERATIONS_PER_IP_PER_HOUR,
            countFunction = { _, startTime ->
                generatedImageRepository.countByIpAddressAndGeneratedAtAfter(identifier, startTime)
            },
            rateLimitMessage =
                "Rate limit exceeded. You can generate up to " +
                    "$MAX_GENERATIONS_PER_IP_PER_HOUR images per hour. Please try again later.",
        )
    }

    override fun processImageGeneration(
        imageFile: MultipartFile,
        request: PublicImageGenerationRequest,
        identifier: String,
    ): PublicImageGenerationResponse {
        val ipAddress = identifier

        val openAIRequest = createOpenAIRequest(request)
        logger.debug("Generated OpenAI request: {}", openAIRequest)

        // Generate images using OpenAI service (get raw bytes)
        val imageEditResponse = openAIImageService.editImageBytes(imageFile, openAIRequest)

        // Store each generated image and create database records
        val generatedImages =
            imageEditResponse.imageBytes.mapIndexed { index, imageBytes ->
                // Generate filename for this image
                val filename = "${UUID.randomUUID()}_generated_${index + 1}.png"

                // Store the image bytes
                imageStorageService.storeImageBytes(imageBytes, filename, ImageType.PUBLIC)

                // Create and save the database record
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
            generatedImages.map { generatedImage ->
                storagePathService.getImageUrl(ImageType.PUBLIC, generatedImage.filename)
            }

        val imageIds = generatedImages.mapNotNull { it.id }

        logger.info("Successfully generated ${imageUrls.size} images for public user")

        return PublicImageGenerationResponse(
            imageUrls = imageUrls,
            generatedImageIds = imageIds,
        )
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
