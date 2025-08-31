package com.jotoai.voenix.shop.image.api

import com.jotoai.voenix.shop.image.api.dto.ImageType
import java.nio.file.Path

/**
 * Public API for storage path management in the image module.
 * Provides centralized storage path functionality for other modules.
 *
 * @deprecated Use ImageService instead. This interface will be removed in a future version.
 * @see ImageService
 */
@Deprecated(
    message = "Use ImageService instead for a unified API - only getImageUrl is exposed as getUrl()",
    replaceWith = ReplaceWith("ImageService", "com.jotoai.voenix.shop.image.api.ImageService"),
    level = DeprecationLevel.WARNING,
)
interface StoragePathService {
    /**
     * Gets the complete URL for a specific image file.
     *
     * @param imageType The type of image
     * @param filename The filename
     * @return The complete URL path to access this specific image
     */
    fun getImageUrl(
        imageType: ImageType,
        filename: String,
    ): String

    /**
     * Gets the physical file system path for storing files of the given image type.
     * This is the absolute path where files should be written to disk.
     *
     * @param imageType The type of image
     * @return The absolute Path where files of this type should be stored
     */
    fun getPhysicalPath(imageType: ImageType): Path

    /**
     * Gets the physical file system path for a specific file.
     *
     * @param imageType The type of image
     * @param filename The filename
     * @return The absolute Path to the specific file
     */
    fun getPhysicalFilePath(
        imageType: ImageType,
        filename: String,
    ): Path

    /**
     * Gets the URL path for accessing images of the given type via HTTP.
     * This is the path that should be used in API responses and web URLs.
     *
     * @param imageType The type of image
     * @return The URL path prefix for this image type
     */
    fun getUrlPath(imageType: ImageType): String

    /**
     * Checks if the given image type is publicly accessible via static URL mapping.
     *
     * @param imageType The type of image
     * @return true if publicly accessible, false if API-controlled access
     */
    fun isPubliclyAccessible(imageType: ImageType): Boolean

    /**
     * Gets all configured image types.
     *
     * @return Set of all configured ImageType values
     */
    fun getAllConfiguredImageTypes(): Set<ImageType>

    /**
     * Finds the ImageType for a given filename by checking which directory contains it.
     * This is useful for operations where only the filename is known.
     *
     * @param filename The filename to search for
     * @return The ImageType if found, null otherwise
     */
    fun findImageTypeByFilename(filename: String): ImageType?

    /**
     * Gets the storage root path.
     *
     * @return The absolute Path to the storage root directory
     */
    fun getStorageRoot(): Path
}
