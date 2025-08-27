package com.jotoai.voenix.shop.image.web

import com.jotoai.voenix.shop.image.api.dto.CropAreaUtils
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.openai.api.ImageGenerationService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

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
        @RequestPart("image") imageFile: MultipartFile,
        @RequestParam("promptId") promptId: Long,
        @RequestParam("cropX", required = false) cropX: Double?,
        @RequestParam("cropY", required = false) cropY: Double?,
        @RequestParam("cropWidth", required = false) cropWidth: Double?,
        @RequestParam("cropHeight", required = false) cropHeight: Double?,
    ): PublicImageGenerationResponse {
        logger.info { "Received public image generation request for prompt ID: $promptId" }

        // Create crop area if all crop parameters are provided
        val cropArea = CropAreaUtils.createIfPresent(cropX, cropY, cropWidth, cropHeight)

        val generationRequest =
            PublicImageGenerationRequest(
                promptId = promptId,
                n = 4,
                cropArea = cropArea,
            )

        val clientIP = "127.0.0.1" // TODO: Extract real IP address
        return imageGenerationService.generatePublicImage(generationRequest, clientIP, imageFile)
    }
}
