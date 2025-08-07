package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.ImageGenerationService
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Implementation of ImageGenerationService that delegates to existing generation services.
 */
@Service
class ImageGenerationServiceImpl(
    private val publicImageGenerationService: PublicImageGenerationService,
    private val userImageGenerationService: UserImageGenerationService,
) : ImageGenerationService {

    override fun generatePublicImage(
        request: PublicImageGenerationRequest,
        ipAddress: String
    ): PublicImageGenerationResponse {
        return publicImageGenerationService.generateImageForPublic(request, ipAddress)
    }

    override fun generateUserImage(
        promptId: Long,
        uploadedImageUuid: UUID?,
        userId: Long
    ): String {
        return userImageGenerationService.generateImageForUser(promptId, uploadedImageUuid, userId)
    }

    override fun isRateLimited(userId: Long?, ipAddress: String?): Boolean {
        return when {
            userId != null -> userImageGenerationService.isUserRateLimited(userId)
            ipAddress != null -> publicImageGenerationService.isIpRateLimited(ipAddress)
            else -> true
        }
    }
}