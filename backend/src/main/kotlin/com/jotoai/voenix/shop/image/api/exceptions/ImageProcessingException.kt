package com.jotoai.voenix.shop.image.api.exceptions

/**
 * Exception thrown when there are issues with image processing operations.
 */
class ImageProcessingException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
