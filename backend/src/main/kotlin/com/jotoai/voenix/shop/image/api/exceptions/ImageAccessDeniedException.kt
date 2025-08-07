package com.jotoai.voenix.shop.image.api.exceptions

/**
 * Exception thrown when access to an image is denied.
 */
class ImageAccessDeniedException(
    message: String,
    val userId: Long? = null,
    val resourceId: String? = null,
) : RuntimeException(message)
