package com.jotoai.voenix.shop.api.user

import com.jotoai.voenix.shop.domain.images.dto.ImageType
import com.jotoai.voenix.shop.domain.images.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.domain.images.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.domain.images.service.ImageService
import com.jotoai.voenix.shop.domain.images.service.UserImageGenerationService
import com.jotoai.voenix.shop.domain.users.service.UserService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
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
@PreAuthorize("hasRole('USER')")
class UserImageController(
    private val userImageGenerationService: UserImageGenerationService,
    private val imageService: ImageService,
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

        // Build request from form data
        val generationRequest =
            PublicImageGenerationRequest(
                promptId = promptId,
                n = 4,
            )

        return userImageGenerationService.generateImage(imageFile, generationRequest, user.id)
    }

    @GetMapping("/{filename}")
    fun getImage(
        @PathVariable filename: String,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<ByteArray> {
        val user = userService.getUserByEmail(userDetails.username)
        logger.info("User ${user.id} retrieving image: $filename")

        // For now, we'll retrieve the image similar to the public endpoint
        // In a future iteration, we might want to verify the user owns this image
        val (imageBytes, contentType) = imageService.getImageData(filename, ImageType.PRIVATE)

        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"$filename\"")
            .body(imageBytes)
    }
}
