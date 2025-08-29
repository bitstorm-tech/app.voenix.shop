package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.application.api.exception.BadRequestException
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
    fun validateImageFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw BadRequestException("Image file is required")
        }
        validateFileSize(file.size, MAX_FILE_SIZE)
        validateContentType(file.contentType, ALLOWED_CONTENT_TYPES, "JPEG, PNG, WebP")
    }

    private fun validateFileSize(
        size: Long,
        maxSize: Long,
    ) {
        if (size > maxSize) {
            throw BadRequestException("Image file size must be less than ${bytesToMegabytes(maxSize)}MB")
        }
    }

    private fun validateContentType(
        contentType: String?,
        allowedTypes: Set<String>,
        formatDescription: String,
    ) {
        val normalizedContentType = contentType?.lowercase() ?: ""
        if (normalizedContentType !in allowedTypes) {
            throw BadRequestException("Invalid image format. Allowed formats: $formatDescription")
        }
    }

    /**
     * Validates an image file for a specific image type.
     */
    fun validateImageFile(
        file: MultipartFile,
        imageType: ImageType,
    ) {
        if (file.isEmpty) {
            throw BadRequestException("Image file is required")
        }
        validateTypedFileSize(file.size, imageType)
        validateTypedContentType(file.contentType, imageType)
    }

    private fun validateTypedFileSize(
        size: Long,
        imageType: ImageType,
    ) {
        if (size > imageType.maxFileSize) {
            val maxSizeMB = bytesToMegabytes(imageType.maxFileSize)
            throw BadRequestException(
                "Image file size must be less than ${maxSizeMB}MB for ${imageType.name}",
            )
        }
    }

    private fun validateTypedContentType(
        contentType: String?,
        imageType: ImageType,
    ) {
        val normalizedContentType = contentType?.lowercase() ?: ""
        if (normalizedContentType !in imageType.allowedContentTypes) {
            throw BadRequestException(
                "Invalid image format for ${imageType.name}. Allowed formats: ${imageType.allowedContentTypes}",
            )
        }
    }
}
