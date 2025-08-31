package com.jotoai.voenix.shop.image.api

import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.image.api.dto.ImageType
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.multipart.MultipartFile

/**
 * Image storage and access interface.
 * Handles file operations and serving images to clients.
 */
interface ImageStorage {
    fun storeFile(
        file: MultipartFile,
        imageType: ImageType,
        cropArea: CropArea? = null,
    ): String

    fun storeFile(
        bytes: ByteArray,
        originalFilename: String,
        imageType: ImageType,
    ): String

    fun loadFileAsBytes(
        filename: String,
        imageType: ImageType,
    ): ByteArray

    fun deleteFile(
        filename: String,
        imageType: ImageType,
    ): Boolean

    fun getImageData(
        filename: String,
        userId: Long? = null,
    ): Pair<ByteArray, String>

    fun serveUserImage(
        filename: String,
        userId: Long,
    ): ResponseEntity<Resource>
}
