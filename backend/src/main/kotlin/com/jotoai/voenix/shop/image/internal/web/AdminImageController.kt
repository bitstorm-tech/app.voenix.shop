package com.jotoai.voenix.shop.image.internal.web

import com.jotoai.voenix.shop.application.ResourceNotFoundException
import com.jotoai.voenix.shop.image.CropArea
import com.jotoai.voenix.shop.image.ImageData
import com.jotoai.voenix.shop.image.ImageInfo
import com.jotoai.voenix.shop.image.ImageMetadata
import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.image.ImageType
import com.jotoai.voenix.shop.user.UserService
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/admin/images")
@PreAuthorize("hasRole('ADMIN')")
class AdminImageController(
    private val imageService: ImageService,
    private val userService: UserService,
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Suppress("LongParameterList")
    fun uploadImage(
        @RequestPart("file") file: MultipartFile,
        @RequestParam(required = false) imageType: ImageType = ImageType.PRIVATE,
        @RequestParam(required = false) cropX: Double?,
        @RequestParam(required = false) cropY: Double?,
        @RequestParam(required = false) cropWidth: Double?,
        @RequestParam(required = false) cropHeight: Double?,
        @AuthenticationPrincipal userDetails: UserDetails,
    ): ImageInfo {
        val cropArea = CropArea.fromNullable(cropX, cropY, cropWidth, cropHeight)
        val adminUser =
            userService.getUserByEmail(userDetails.username)
                ?: throw ResourceNotFoundException("User", "email", userDetails.username)

        return imageService.store(
            ImageData.File(file, cropArea),
            ImageMetadata(
                type = imageType,
                userId = adminUser.id,
            ),
        )
    }
}
