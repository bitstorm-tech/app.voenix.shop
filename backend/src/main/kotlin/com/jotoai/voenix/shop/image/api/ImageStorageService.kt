package com.jotoai.voenix.shop.image.api

import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.image.api.dto.ImageType
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

/**
 * Service for image storage and retrieval operations.
 * This interface defines operations for storing, retrieving, and managing image files.
 */
interface ImageStorageService {
    /**
     * Stores a multipart file and returns the stored filename.
     * If cropArea is provided, the image will be cropped before storage.
     */
    fun storeFile(
        file: MultipartFile,
        imageType: ImageType,
        cropArea: CropArea? = null,
    ): String

    /**
     * Stores a byte array as a file and returns the stored filename.
     */
    fun storeFile(
        bytes: ByteArray,
        originalFilename: String,
        imageType: ImageType,
    ): String

    /**
     * Loads a file as a Resource by filename and image type.
     */
    fun loadFileAsResource(
        filename: String,
        imageType: ImageType,
    ): Resource

    /**
     * Loads a file as bytes by filename and image type.
     */
    fun loadFileAsBytes(
        filename: String,
        imageType: ImageType,
    ): ByteArray

    /**
     * Deletes a file by filename and image type.
     */
    fun deleteFile(
        filename: String,
        imageType: ImageType,
    ): Boolean

    /**
     * Checks if a file exists.
     */
    fun fileExists(
        filename: String,
        imageType: ImageType,
    ): Boolean
}
