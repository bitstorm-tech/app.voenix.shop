package com.jotoai.voenix.shop.image.internal.config

import com.jotoai.voenix.shop.image.ImageType
import java.nio.file.Path

/**
 * Configuration class that defines both physical storage paths and URL paths for each ImageType.
 * This centralizes all path definitions and makes the system easily extensible for new image types.
 */
data class StoragePathConfiguration(
    /**
     * The root directory for all file storage (e.g., "/app/storage" or "storage")
     */
    val storageRoot: Path,
    /**
     * Map of ImageType to their respective path configurations
     */
    val pathMappings: Map<ImageType, ImageTypePathConfig>,
)

/**
 * Path configuration for a specific image type, defining both physical storage path
 * and the corresponding URL path for web access.
 */
data class ImageTypePathConfig(
    /**
     * The relative path from storage root where files of this type are stored
     * Example: "private/images" or "public/images/prompt-example-images"
     */
    val relativePath: String,
    /**
     * The URL path prefix for accessing these images via HTTP
     * Example: "/api/user/images" or "/images/prompt-example-images"
     */
    val urlPath: String,
    /**
     * Whether this image type is publicly accessible via static URL mapping
     * (true for images served directly by Spring's ResourceHandler, false for API-controlled access)
     */
    val isPubliclyAccessible: Boolean = false,
)
