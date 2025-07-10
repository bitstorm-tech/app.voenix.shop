package com.jotoai.voenix.shop.images.controller

import com.jotoai.voenix.shop.images.dto.CreateImageRequest
import com.jotoai.voenix.shop.images.dto.ImageDto
import com.jotoai.voenix.shop.images.dto.ImageType
import com.jotoai.voenix.shop.images.service.ImageService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/images")
class ImageController(
    private val imageService: ImageService,
) {
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadImage(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("imageType") imageType: ImageType,
    ): ResponseEntity<ImageDto> {
        val request = CreateImageRequest(imageType = imageType)
        val image = imageService.upload(file, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(image)
    }

    @GetMapping("/{filename}")
    fun getImage(
        @PathVariable filename: String,
    ): ResponseEntity<ByteArray> {
        val (imageData, contentType) = imageService.getImageData(filename)
        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType(contentType))
            .body(imageData)
    }

    @DeleteMapping("/{filename}")
    fun deleteImage(
        @PathVariable filename: String,
    ): ResponseEntity<Void> {
        imageService.delete(filename)
        return ResponseEntity.noContent().build()
    }
}
