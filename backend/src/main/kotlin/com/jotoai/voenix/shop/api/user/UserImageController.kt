package com.jotoai.voenix.shop.api.user

import com.jotoai.voenix.shop.image.api.ImageAccessService
import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.image.api.ImageGenerationService
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.user.api.UserQueryService
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/user/images")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
class UserImageController(
    private val imageGenerationService: ImageGenerationService,
    private val imageFacade: ImageFacade,
    private val imageAccessService: ImageAccessService,
    private val userQueryService: UserQueryService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserImageController::class.java)
    }

    @PostMapping("/generate", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun generateImage(
        @RequestPart("image") imageFile: MultipartFile,
        @RequestParam("promptId") promptId: Long,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): PublicImageGenerationResponse {
        val user = userQueryService.getUserByEmail(userDetails.username)
        logger.info("Received authenticated image generation request from user ${user.id} for prompt ID: $promptId")

        val generationRequest =
            PublicImageGenerationRequest(
                promptId = promptId,
                n = 4,
            )

        // First upload the image to get UUID
        val uploadedImage = imageFacade.createUploadedImage(imageFile, user.id)

        // Then generate images using the uploaded image UUID
        val generatedImageIds =
            (1..generationRequest.n).map {
                imageGenerationService.generateUserImage(promptId, uploadedImage.uuid, user.id)
            }

        // Create response with API URLs and parse IDs from generated filenames
        val imageUrls =
            generatedImageIds.map { filename ->
                "/api/user/images/$filename"
            }

        // For now, return empty list for generated image IDs as the public API returns filenames
        return PublicImageGenerationResponse(
            imageUrls = imageUrls,
            generatedImageIds = emptyList(),
        )
    }

    @GetMapping("/{filename}")
    fun getImage(
        @PathVariable filename: String,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<ByteArray> {
        val user = userQueryService.getUserByEmail(userDetails.username)
        logger.info("User ${user.id} retrieving image: $filename")

        // Use the serveUserImage method instead which handles validation internally
        val response = imageAccessService.serveUserImage(filename, user.id)

        // Convert Resource response to ByteArray response for consistency with the API contract
        val resource = response.body!!
        val imageBytes = resource.inputStream.readAllBytes()

        return ResponseEntity
            .ok()
            .headers(response.headers)
            .body(imageBytes)
    }
}
