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
        ipAddress: String,
    ): PublicImageGenerationResponse {
        // PublicImageGenerationService doesn't take IP address separately
        // We need to rework this call
        throw UnsupportedOperationException("Need to implement generateImage call")
    }

    override fun generateUserImage(
        promptId: Long,
        uploadedImageUuid: UUID?,
        userId: Long,
    ): String {
        // UserImageGenerationService uses different method signature
        throw UnsupportedOperationException("Need to implement generateImage call")
    }

    override fun isRateLimited(
        userId: Long?,
        ipAddress: String?,
    ): Boolean =
        when {
            userId != null -> false // TODO: implement rate limiting check
            ipAddress != null -> false // TODO: implement rate limiting check
            else -> true
        }
}
