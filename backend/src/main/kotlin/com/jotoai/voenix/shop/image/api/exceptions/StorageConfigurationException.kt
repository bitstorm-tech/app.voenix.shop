package com.jotoai.voenix.shop.image.api.exceptions

/**
 * Exception thrown when there are issues with storage configuration or setup.
 */
class StorageConfigurationException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
