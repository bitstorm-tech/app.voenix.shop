package com.jotoai.voenix.shop.image.api.exceptions

/**
 * Sealed class hierarchy for all image-related exceptions.
 * This provides a single catch point for all image domain errors.
 */
sealed class ImageException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {
    /**
     * Exception thrown when an image cannot be found.
     * Extends ResourceNotFoundException for framework compatibility
     * while still being part of the ImageException hierarchy.
     */
    class NotFound(
        imageId: String,
    ) : ImageException(
            "Image not found with id: $imageId",
        )

    /**
     * Exception thrown when access to an image is denied.
     */
    class AccessDenied(
        userId: Long? = null,
        resourceId: String? = null,
    ) : ImageException(
            "Access denied to image${resourceId?.let { " $it" } ?: ""}${userId?.let { " for user $it" } ?: ""}",
        )

    /**
     * Exception thrown when there are issues with image processing operations.
     */
    class Processing(
        message: String,
        cause: Throwable? = null,
    ) : ImageException(message, cause)

    /**
     * Exception thrown when there are issues with image storage operations.
     */
    class Storage(
        message: String,
        cause: Throwable? = null,
    ) : ImageException(message, cause)
}
