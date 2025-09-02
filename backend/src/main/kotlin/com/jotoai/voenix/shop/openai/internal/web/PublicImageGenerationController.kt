package com.jotoai.voenix.shop.openai.internal.web

import com.jotoai.voenix.shop.application.internal.service.ClientIpResolver
import com.jotoai.voenix.shop.image.CropArea
import com.jotoai.voenix.shop.openai.internal.dto.ImageGenerationRequest
import com.jotoai.voenix.shop.openai.internal.dto.ImageGenerationResponse
import com.jotoai.voenix.shop.openai.internal.service.OpenAIImageService
import com.jotoai.voenix.shop.openai.internal.web.dto.ImageGenerationForm
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/ai/images")
class PublicImageGenerationController(
    private val openAIImageService: OpenAIImageService,
    private val clientIpResolver: ClientIpResolver,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostMapping("/generate", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun generateImage(
        @ModelAttribute form: ImageGenerationForm,
        @RequestParam("provider", required = false, defaultValue = "OPENAI") provider: String,
        request: HttpServletRequest,
    ): ImageGenerationResponse {
        logger.info { "Public image generation request: promptId=${form.promptId}, provider=$provider" }

        // Create crop area if all crop parameters are provided
        val cropArea = CropArea.fromNullable(form.cropX, form.cropY, form.cropWidth, form.cropHeight)

        val generationRequest =
            ImageGenerationRequest(
                promptId = form.promptId,
                n = 4,
                cropArea = cropArea,
            )

        val clientIP = clientIpResolver.resolve(request)
        return openAIImageService.generatePublicImage(
            generationRequest,
            clientIP,
            form.image,
            OpenAIImageService.AiProvider.valueOf(provider.uppercase()),
        )
    }
}
