package com.jotoai.voenix.shop.image.api.exceptions

/**
 * Exception thrown when there are issues with image storage operations.
 */
class ImageStorageException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
