package com.jotoai.voenix.shop.image

import java.time.OffsetDateTime
import java.util.UUID

/**
 * DTO representing a user's image (both uploaded and generated images).
 * Used by the image module's public API for listing user images.
 */
data class UserImageDto(
    val id: Long,
    val uuid: UUID,
    val filename: String,
    val originalFilename: String?,
    val type: String, // "uploaded" or "generated"
    val contentType: String?,
    val fileSize: Long?,
    val promptId: Long?,
    val promptTitle: String?, // Enriched from prompt module
    val uploadedImageId: Long?,
    val userId: Long,
    val createdAt: OffsetDateTime?,
    val imageUrl: String,
)
