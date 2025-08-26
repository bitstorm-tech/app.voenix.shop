package com.jotoai.voenix.shop.openai.internal.service

import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.openai.api.ImageGenerationService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

/**
 * Implementation of ImageGenerationService that delegates to the ImageFacade.
 * This serves as a bridge between the openai module's API and the image module's functionality.
 */
@Service
class ImageGenerationFacadeImpl(
    private val imageFacade: ImageFacade,
) : ImageGenerationService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun generatePublicImage(
        request: PublicImageGenerationRequest,
        ipAddress: String,
        imageFile: MultipartFile,
    ): PublicImageGenerationResponse {
        logger.info { "Delegating public image generation to ImageFacade" }
        return imageFacade.generatePublicImage(request, ipAddress, imageFile)
    }

    override fun generateUserImage(
        promptId: Long,
        uploadedImageUuid: UUID?,
        userId: Long,
        cropArea: CropArea?,
    ): String {
        logger.info { "Delegating user image generation to ImageFacade" }
        requireNotNull(uploadedImageUuid) { "uploadedImageUuid is required for user image generation" }
        val response = imageFacade.generateUserImageWithIds(promptId, uploadedImageUuid, userId, cropArea)
        return response.imageUrls.firstOrNull() ?: throw RuntimeException("No images generated")
    }

    override fun generateUserImageWithIds(
        promptId: Long,
        uploadedImageUuid: UUID?,
        userId: Long,
        cropArea: CropArea?,
    ): PublicImageGenerationResponse {
        logger.info { "Delegating user image generation with IDs to ImageFacade" }
        requireNotNull(uploadedImageUuid) { "uploadedImageUuid is required for user image generation" }
        return imageFacade.generateUserImageWithIds(promptId, uploadedImageUuid, userId, cropArea)
    }

    override fun isRateLimited(
        userId: Long?,
        ipAddress: String?,
    ): Boolean {
        // Implementation would check rate limits based on userId or ipAddress
        // For now, delegate to existing rate limit logic in ImageFacade
        // This is a simplified implementation - in practice, you'd call specific rate limit methods
        logger.debug { "Rate limit check for userId=$userId, ipAddress=$ipAddress" }
        return false // Let the actual generation methods handle rate limiting with proper exceptions
    }
}
