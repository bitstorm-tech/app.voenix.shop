package com.jotoai.voenix.shop.image

import java.time.LocalDateTime
import java.util.UUID

/**
 * Common interface for image information
 */
interface ImageInfo {
    val filename: String
    val imageType: ImageType
}

/**
 * DTO for uploaded images with complete metadata
 */
data class UploadedImageDto(
    override val filename: String,
    override val imageType: ImageType,
    val id: Long? = null,
    val uuid: UUID,
    val originalFilename: String,
    val contentType: String,
    val fileSize: Long,
    val uploadedAt: LocalDateTime,
) : ImageInfo

/**
 * DTO for generated images
 */
data class GeneratedImageDto(
    override val filename: String,
    override val imageType: ImageType,
    val id: Long? = null,
    val promptId: Long,
    val userId: Long? = null,
    val generatedAt: LocalDateTime,
    val ipAddress: String? = null,
) : ImageInfo
