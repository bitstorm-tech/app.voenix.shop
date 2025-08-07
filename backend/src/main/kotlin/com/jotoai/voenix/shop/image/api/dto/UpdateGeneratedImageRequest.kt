package com.jotoai.voenix.shop.image.api.dto

/**
 * Request DTO for updating generated image metadata.
 * All fields are optional to allow partial updates.
 */
data class UpdateGeneratedImageRequest(
    val promptId: Long? = null,
    val userId: Long? = null,
    val ipAddress: String? = null,
)
