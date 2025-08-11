package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.ImageAccessService
import com.jotoai.voenix.shop.image.api.dto.ImageFormat
import com.jotoai.voenix.shop.image.api.dto.ImageType
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

/**
 * Implementation of ImageAccessService that serves images with proper access control.
 */
@Service
class ImageAccessServiceImpl(
    private val imageManagementService: ImageManagementService,
) : ImageAccessService {
    override fun getImageData(
        filename: String,
        userId: Long?,
    ): Pair<ByteArray, String> = imageManagementService.getImageData(filename, userId)

    override fun serveImage(
        filename: String,
        imageType: ImageType,
        format: ImageFormat?,
    ): ResponseEntity<Resource> {
        val (imageData, contentType) = imageManagementService.getImageData(filename, imageType)
        return createImageResponse(imageData, contentType, filename)
    }

    override fun serveUserImage(
        filename: String,
        userId: Long,
        format: ImageFormat?,
    ): ResponseEntity<Resource> {
        val (imageData, contentType) = imageManagementService.validateAccessAndGetImageData(filename, userId)
        return createImageResponse(imageData, contentType, filename)
    }

    override fun servePublicImage(
        filename: String,
        format: ImageFormat?,
    ): ResponseEntity<Resource> {
        val (imageData, contentType) = imageManagementService.getImageData(filename)
        return createImageResponse(imageData, contentType, filename)
    }

    private fun createImageResponse(
        imageData: ByteArray,
        contentType: String,
        filename: String,
    ): ResponseEntity<Resource> =
        ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"$filename\"")
            .contentType(MediaType.parseMediaType(contentType))
            .body(ByteArrayResource(imageData))
}
