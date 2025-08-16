package com.jotoai.voenix.shop.api.user

import com.jotoai.voenix.shop.image.api.ImageAccessService
import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.image.api.ImageGenerationService
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.user.api.UserService
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
    private val userService: UserService,
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
        val user = userService.getUserByEmail(userDetails.username)
        logger.info("Received authenticated image generation request from user ${user.id} for prompt ID: $promptId")

        // First upload the image to get UUID
        val uploadedImage = imageFacade.createUploadedImage(imageFile, user.id)
        logger.info("Uploaded image for user ${user.id} with UUID: ${uploadedImage.uuid}")

        // Generate all 4 images in one call and get the complete response with IDs
        val response = imageGenerationService.generateUserImageWithIds(promptId, uploadedImage.uuid, user.id)

        logger.info(
            "Generated ${response.generatedImageIds.size} images with IDs: " +
                "${response.generatedImageIds} for user ${user.id}",
        )

        return response
    }

    @GetMapping("/{filename}")
    fun getImage(
        @PathVariable filename: String,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<ByteArray> {
        val user = userService.getUserByEmail(userDetails.username)
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
