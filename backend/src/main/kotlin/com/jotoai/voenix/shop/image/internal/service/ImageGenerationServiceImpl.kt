package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.ImageGenerationService
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/**
 * Implementation of ImageGenerationService that delegates to the ImageGenerationOrchestrator.
 * This facade provides a clean API for image generation while the orchestrator handles
 * the coordination between different generation services.
 */
@Service
class ImageGenerationServiceImpl(
    private val imageGenerationOrchestrator: ImageGenerationOrchestrator,
) : ImageGenerationService {
    override fun generatePublicImage(
        request: PublicImageGenerationRequest,
        ipAddress: String,
        imageFile: MultipartFile,
    ): PublicImageGenerationResponse {
        // Delegate the actual work to the orchestrator which knows how to handle the MultipartFile
        return imageGenerationOrchestrator.generatePublicImage(imageFile, request)
    }

    override fun generateUserImage(
        promptId: Long,
        uploadedImageUuid: UUID?,
        userId: Long,
    ): String {
        // This method has design limitations:
        // 1. It can only return a single filename but generation typically produces multiple images
        // 2. The current API design doesn't align well with the multi-image generation capability

        requireNotNull(uploadedImageUuid) { "Uploaded image UUID is required for user image generation" }

        try {
            // Delegate to orchestrator for UUID-based generation
            val response =
                imageGenerationOrchestrator.generateUserImageFromUpload(
                    promptId = promptId,
                    userId = userId,
                )

            // Return the first generated image filename
            // Note: This is a design limitation - we can only return one filename
            return response.imageUrls.firstOrNull()?.substringAfterLast("/")
                ?: throw RuntimeException("No images were generated")
        } catch (e: Exception) {
            throw RuntimeException("Failed to generate user image: ${e.message}", e)
        }
    }

    override fun isRateLimited(
        userId: Long?,
        ipAddress: String?,
    ): Boolean = imageGenerationOrchestrator.isRateLimited(userId, ipAddress)
}
