package com.jotoai.voenix.shop.api.public.images

import com.jotoai.voenix.shop.domain.images.service.ImageService
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/images")
class PublicImageController(
    private val imageService: ImageService,
) {
    @GetMapping("/{filename}/download")
    fun downloadImage(
        @PathVariable filename: String,
    ): ResponseEntity<Resource> {
        val (imageData, contentType) = imageService.getImageData(filename)
        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
            .contentType(MediaType.parseMediaType(contentType))
            .body(ByteArrayResource(imageData))
    }
}
