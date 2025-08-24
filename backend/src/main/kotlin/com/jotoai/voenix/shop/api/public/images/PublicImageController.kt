package com.jotoai.voenix.shop.api.public.images

import com.jotoai.voenix.shop.image.api.ImageAccessService
import com.jotoai.voenix.shop.image.api.ImageGenerationService
import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
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
    private val imageAccessService: ImageAccessService,
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
        val cropArea =
            if (cropX != null && cropY != null && cropWidth != null && cropHeight != null) {
                CropArea(x = cropX, y = cropY, width = cropWidth, height = cropHeight)
            } else {
                null
            }

        val generationRequest =
            PublicImageGenerationRequest(
                promptId = promptId,
                n = 4,
                cropArea = cropArea,
            )

        val clientIP = "127.0.0.1" // TODO: Extract real IP address
        return imageGenerationService.generatePublicImage(generationRequest, clientIP, imageFile)
    }

    @GetMapping("/{filename}")
    fun getImage(
        @PathVariable filename: String,
    ): ResponseEntity<Resource> {
        logger.info { "Retrieving public image: $filename" }

        return imageAccessService.servePublicImage(filename)
    }
}
