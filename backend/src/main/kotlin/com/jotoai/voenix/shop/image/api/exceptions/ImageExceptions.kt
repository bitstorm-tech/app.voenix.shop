package com.jotoai.voenix.shop.image.api.exceptions

import com.jotoai.voenix.shop.application.api.exception.ResourceNotFoundException

/**
 * Exception thrown when an image cannot be found.
 */
class ImageNotFoundException(imageId: String) 
    : ResourceNotFoundException("Image", "id", imageId)

/**
 * Sealed class hierarchy for all other image-related exceptions.
 */
sealed class ImageException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {
    
    /**
     * Exception thrown when access to an image is denied.
     */
    class AccessDenied(
        userId: Long? = null, 
        resourceId: String? = null
    ) : ImageException(
        "Access denied to image${resourceId?.let { " $it" } ?: ""}${userId?.let { " for user $it" } ?: ""}"
    )
    
    /**
     * Exception thrown when there are issues with image processing operations.
     */
    class Processing(
        message: String, 
        cause: Throwable? = null
    ) : ImageException(message, cause)
    
    /**
     * Exception thrown when there are issues with image storage operations.
     */
    class Storage(
        message: String, 
        cause: Throwable? = null
    ) : ImageException(message, cause)
}
