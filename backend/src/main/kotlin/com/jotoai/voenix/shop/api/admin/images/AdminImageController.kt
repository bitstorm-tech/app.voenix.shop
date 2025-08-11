package com.jotoai.voenix.shop.api.admin.images

import com.jotoai.voenix.shop.image.api.ImageAccessService
import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.image.api.dto.CreateImageRequest
import com.jotoai.voenix.shop.image.api.dto.ImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import jakarta.validation.Valid
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/images")
@PreAuthorize("hasRole('ADMIN')")
class AdminImageController(
    private val imageFacade: ImageFacade,
    private val imageAccessService: ImageAccessService,
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadImage(
        @Valid request: CreateImageRequest,
    ): ImageDto = imageFacade.createImage(request)

    @GetMapping("/{filename}/download")
    fun downloadImage(
        @PathVariable filename: String,
    ): ResponseEntity<Resource> = imageAccessService.serveImage(filename, ImageType.PUBLIC)

    @DeleteMapping("/{filename}")
    fun deleteImage(): Unit = throw UnsupportedOperationException("Delete via ImageFacade not implemented yet")
}
