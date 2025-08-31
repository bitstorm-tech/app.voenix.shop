package com.jotoai.voenix.shop.image.internal.exceptions

/**
 * Internal exceptions for the image module that are not part of the public API.
 */
sealed class InternalImageException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    
    /**
     * Exception thrown when image quota limits are exceeded.
     */
    class QuotaExceeded(
        userId: Long? = null,
        ipAddress: String? = null
    ) : InternalImageException(
        buildString {
            append("Image quota exceeded")
            if (userId != null) append(" for user $userId")
            if (ipAddress != null) append(" from IP $ipAddress")
        }
    )
    
    /**
     * Exception thrown when there are issues with storage configuration or setup.
     */
    class StorageConfiguration(
        message: String,
        cause: Throwable? = null
    ) : InternalImageException(message, cause)
}
