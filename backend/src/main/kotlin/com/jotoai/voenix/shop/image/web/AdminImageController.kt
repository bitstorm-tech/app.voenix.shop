package com.jotoai.voenix.shop.image.web

import com.fasterxml.jackson.databind.ObjectMapper
import com.jotoai.voenix.shop.image.api.ImageData
import com.jotoai.voenix.shop.image.api.ImageMetadata
import com.jotoai.voenix.shop.image.api.ImageService
import com.jotoai.voenix.shop.image.api.dto.ImageInfo
import com.jotoai.voenix.shop.image.internal.web.dto.CreateImageRequest
import com.jotoai.voenix.shop.user.api.UserService
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/admin/images")
@PreAuthorize("hasRole('ADMIN')")
class AdminImageController(
    private val imageService: ImageService,
    private val objectMapper: ObjectMapper,
    private val userService: UserService,
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadImage(
        @RequestPart("file") file: MultipartFile,
        @RequestPart("request") requestPart: String,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ImageInfo {
        // Parse the request part from JSON
        val createImageRequest = objectMapper.readValue(requestPart, CreateImageRequest::class.java)

        // Get the actual admin user ID from security context
        val adminUser = userService.getUserByEmail(userDetails.username)

        // Upload the image using the image service with the specified imageType
        val uploadedImage =
            imageService.store(
                ImageData.File(file, createImageRequest.cropArea),
                ImageMetadata(
                    type = createImageRequest.imageType,
                    userId = adminUser.id,
                ),
            )

        // Return the uploaded image (already implements ImageInfo)
        return uploadedImage
    }
}
