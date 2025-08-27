package com.jotoai.voenix.shop.image.web

import com.jotoai.voenix.shop.image.api.ImageAccessService
import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.image.api.dto.CropAreaUtils
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.user.api.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.io.Resource
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
    private val imageFacade: ImageFacade,
    private val imageAccessService: ImageAccessService,
    private val userService: UserService,
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
        @AuthenticationPrincipal userDetails: UserDetails,
    ): PublicImageGenerationResponse {
        val user = userService.getUserByEmail(userDetails.username)
        logger.info { "Received authenticated image generation request from user ${user.id} for prompt ID: $promptId" }

        // Create crop area if all crop parameters are provided
        val cropArea = CropAreaUtils.createIfPresent(cropX, cropY, cropWidth, cropHeight)

        // First upload the image to get UUID
        val uploadedImage = imageFacade.createUploadedImage(imageFile, user.id)
        logger.info { "Uploaded image for user ${user.id} with UUID: ${uploadedImage.uuid}" }

        // Generate all 4 images in one call and get the complete response with IDs
        val response = imageFacade.generateUserImageWithIds(promptId, uploadedImage.uuid, user.id, cropArea)

        logger.info {
            "Generated ${response.generatedImageIds.size} images with IDs: " +
                "${response.generatedImageIds} for user ${user.id}"
        }

        return response
    }

    @GetMapping("/{filename}")
    fun getImage(
        @PathVariable filename: String,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ResponseEntity<Resource> {
        val user = userService.getUserByEmail(userDetails.username)
        logger.info { "User ${user.id} retrieving image: $filename" }

        // Delegate to access service which validates access and streams the resource
        return imageAccessService.serveUserImage(filename, user.id)
    }

}
