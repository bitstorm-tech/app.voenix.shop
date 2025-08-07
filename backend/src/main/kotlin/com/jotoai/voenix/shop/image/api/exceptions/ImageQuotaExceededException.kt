package com.jotoai.voenix.shop.image.api.exceptions

/**
 * Exception thrown when image quota limits are exceeded.
 */
class ImageQuotaExceededException(
    message: String,
    val userId: Long? = null,
    val ipAddress: String? = null,
) : RuntimeException(message)
