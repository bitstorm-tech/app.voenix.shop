package com.jotoai.voenix.shop.api.admin.images

import com.jotoai.voenix.shop.domain.images.dto.CreateImageRequest
import com.jotoai.voenix.shop.domain.images.dto.ImageDto
import com.jotoai.voenix.shop.domain.images.service.ImageService
import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
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
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadImage(
        @RequestParam("file") file: MultipartFile,
        @RequestPart("request") @Valid request: CreateImageRequest,
    ): ImageDto = imageService.store(file, request)

    @DeleteMapping("/{filename}")
    fun deleteImage(
        @PathVariable filename: String,
    ) {
        imageService.delete(filename)
    }
}
