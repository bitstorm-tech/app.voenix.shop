package com.jotoai.voenix.shop.image.web

import com.jotoai.voenix.shop.image.api.dto.CropAreaUtils
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.openai.api.ImageGenerationService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import com.jotoai.voenix.shop.image.web.dto.ImageGenerationForm

@RestController
@RequestMapping("/api/public/images")
class PublicImageController(
    private val imageGenerationService: ImageGenerationService,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostMapping("/generate", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun generateImage(
        @ModelAttribute form: ImageGenerationForm,
        request: HttpServletRequest,
    ): PublicImageGenerationResponse {
        logger.info { "Received public image generation request for prompt ID: ${form.promptId}" }

        // Create crop area if all crop parameters are provided
        val cropArea = CropAreaUtils.createIfPresent(form.cropX, form.cropY, form.cropWidth, form.cropHeight)

        val generationRequest =
            PublicImageGenerationRequest(
                promptId = form.promptId,
                n = 4,
                cropArea = cropArea,
            )

        val clientIP = extractClientIp(request)
        return imageGenerationService.generatePublicImage(generationRequest, clientIP, form.image)
    }

    private fun extractClientIp(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(',').first().trim()
        }
        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp
        }
        return request.remoteAddr
    }
}
