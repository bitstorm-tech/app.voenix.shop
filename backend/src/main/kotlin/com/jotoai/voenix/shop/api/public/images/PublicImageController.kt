package com.jotoai.voenix.shop.api.public.images

import com.jotoai.voenix.shop.domain.images.dto.ImageType
import com.jotoai.voenix.shop.domain.images.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.domain.images.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.domain.images.service.ImageService
import com.jotoai.voenix.shop.domain.images.service.PublicImageGenerationService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
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
    private val publicImageGenerationService: PublicImageGenerationService,
    private val imageService: ImageService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PublicImageController::class.java)
    }

    @PostMapping("/generate", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun generateImage(
        @RequestPart("image") imageFile: MultipartFile,
        @RequestParam("promptId") promptId: Long,
    ): PublicImageGenerationResponse {
        logger.info("Received public image generation request for prompt ID: $promptId")

        val generationRequest =
            PublicImageGenerationRequest(
                promptId = promptId,
                n = 4,
            )

        return publicImageGenerationService.generateImage(imageFile, generationRequest)
    }

    @GetMapping("/{filename}")
    fun getImage(
        @PathVariable filename: String,
    ): ResponseEntity<ByteArray> {
        logger.info("Retrieving public image: $filename")

        val (imageBytes, contentType) = imageService.getImageData(filename, ImageType.PRIVATE)

        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"$filename\"")
            .body(imageBytes)
    }
}
