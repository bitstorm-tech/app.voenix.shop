package com.jotoai.voenix.shop.image.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.jotoai.voenix.shop.image.api.ImageAccessService
import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.image.api.dto.CreateImageRequest
import com.jotoai.voenix.shop.image.api.dto.ImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.SimpleImageDto
import com.jotoai.voenix.shop.user.api.UserService
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/admin/images")
@PreAuthorize("hasRole('ADMIN')")
class AdminImageController(
    private val imageFacade: ImageFacade,
    private val imageAccessService: ImageAccessService,
    private val objectMapper: ObjectMapper,
    private val userService: UserService,
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadImage(
        @RequestPart("file") file: MultipartFile,
        @RequestPart("request") requestPart: String,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ImageDto {
        // Parse the request part from JSON
        val createImageRequest = objectMapper.readValue(requestPart, CreateImageRequest::class.java)

        // Get the actual admin user ID from security context
        val adminUser = userService.getUserByEmail(userDetails.username)

        // Upload the image using the facade's multipart method with the specified imageType
        val uploadedImage = imageFacade.createUploadedImage(file, adminUser.id, createImageRequest.imageType)

        // Convert to the expected return type
        return SimpleImageDto(
            filename = uploadedImage.filename,
            imageType = uploadedImage.imageType,
        )
    }

    @GetMapping("/{filename}/download")
    fun downloadImage(
        @PathVariable filename: String,
    ): ResponseEntity<Resource> = imageAccessService.serveImage(filename, ImageType.PUBLIC)

    @DeleteMapping("/{filename}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteImage(
        @PathVariable filename: String,
        @AuthenticationPrincipal userDetails: UserDetails,
    ) {
        val adminUser = userService.getUserByEmail(userDetails.username)

        // TODO: Implement admin image deletion
        // This requires either:
        // 1. Adding a deleteImageByFilename method to ImageFacade, or
        // 2. Finding the uploaded image by filename first to get the UUID/userId, then using existing deleteUploadedImage
        // For now, throwing UnsupportedOperationException to indicate this needs proper implementation
        throw UnsupportedOperationException("Admin image deletion not yet implemented - requires filename-based deletion in ImageFacade")
    }
}
