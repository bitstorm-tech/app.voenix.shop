package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.dto.ImageType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

/**
 * Centralized validation service for all image-related operations.
 * Provides consistent validation logic and error messages across the application.
 */
@Service
class ImageValidationService {
    companion object {
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024L // 10MB
        private const val BYTES_PER_KB = 1024L
        private const val KB_PER_MB = 1024L
        private val ALLOWED_CONTENT_TYPES = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")

        private fun bytesToMegabytes(bytes: Long): Long = bytes / (BYTES_PER_KB * KB_PER_MB)
    }

    /**
     * Validates an uploaded image file.
     */
    fun validateImageFile(
        file: MultipartFile,
        imageType: ImageType? = null,
    ) {
        require(!file.isEmpty) { "Image file is required" }

        val maxSize = imageType?.maxFileSize ?: MAX_FILE_SIZE
        val allowedTypes = imageType?.allowedContentTypes ?: ALLOWED_CONTENT_TYPES

        require(file.size <= maxSize) {
            val maxSizeMB = bytesToMegabytes(maxSize)
            val typeContext = imageType?.let { " for ${it.name}" } ?: ""
            "File size must be less than ${maxSizeMB}MB$typeContext"
        }

        require(file.contentType?.lowercase() in allowedTypes) {
            val typeContext = imageType?.let { " for ${it.name}" } ?: ""
            "Invalid format$typeContext. Allowed: ${allowedTypes.joinToString()}"
        }
    }
}
