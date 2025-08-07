package com.jotoai.voenix.shop.image.api.dto

import java.time.LocalDateTime
import java.util.UUID

/**
 * Base class for image DTOs
 */
sealed class ImageDto(
    open val filename: String,
    open val imageType: ImageType,
)

/**
 * DTO for uploaded images with complete metadata
 */
data class UploadedImageDto(
    override val filename: String,
    override val imageType: ImageType,
    val uuid: UUID,
    val originalFilename: String,
    val contentType: String,
    val fileSize: Long,
    val uploadedAt: LocalDateTime,
) : ImageDto(filename, imageType)

/**
 * DTO for generated images
 */
data class GeneratedImageDto(
    override val filename: String,
    override val imageType: ImageType,
    val promptId: Long,
    val userId: Long? = null,
    val generatedAt: LocalDateTime,
    val ipAddress: String? = null,
) : ImageDto(filename, imageType)

/**
 * Simple ImageDto for backward compatibility
 */
data class SimpleImageDto(
    override val filename: String,
    override val imageType: ImageType,
) : ImageDto(filename, imageType)
