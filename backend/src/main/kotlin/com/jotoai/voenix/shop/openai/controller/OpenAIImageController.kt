package com.jotoai.voenix.shop.openai.controller

import com.jotoai.voenix.shop.openai.dto.CreateImageEditRequest
import com.jotoai.voenix.shop.openai.dto.ImageEditResponse
import com.jotoai.voenix.shop.openai.service.OpenAIImageService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/openai/images")
class OpenAIImageController(
    private val openAIImageService: OpenAIImageService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(OpenAIImageController::class.java)
    }

    @PostMapping("/edit", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun editImage(
        @RequestParam("image") imageFile: MultipartFile,
        @Valid @ModelAttribute request: CreateImageEditRequest,
    ): ResponseEntity<ImageEditResponse> {
        logger.info("Received image edit request - promptId: ${request.promptId}, size: ${request.size}, quality: ${request.quality}")

        // Validate image file
        if (imageFile.isEmpty) {
            throw IllegalArgumentException("Image file is required")
        }

        val contentType = imageFile.contentType
        if (contentType == null || !contentType.startsWith("image/")) {
            throw IllegalArgumentException("File must be an image")
        }

        val response = openAIImageService.editImage(imageFile, request)

        logger.info("Successfully generated ${response.imageFilenames.size} images")

        return ResponseEntity.ok(response)
    }
}
