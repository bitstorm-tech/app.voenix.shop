package com.jotoai.voenix.shop.domain.images.service

import com.jotoai.voenix.shop.domain.images.config.StoragePathConfiguration
import com.jotoai.voenix.shop.domain.images.dto.ImageType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Service responsible for providing centralized storage path management.
 * Handles both physical file system paths for storage and URL paths for web access.
 */
@Service
class StoragePathService(
    private val storagePathConfiguration: StoragePathConfiguration,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(StoragePathService::class.java)
    }

    init {
        logger.info("Initializing StoragePathService with storage root: ${storagePathConfiguration.storageRoot}")
        createDirectories()
    }

    /**
     * Gets the physical file system path for storing files of the given image type.
     * This is the absolute path where files should be written to disk.
     *
     * @param imageType The type of image
     * @return The absolute Path where files of this type should be stored
     */
    fun getPhysicalPath(imageType: ImageType): Path {
        val pathConfig = getPathConfig(imageType)
        return storagePathConfiguration.storageRoot.resolve(pathConfig.relativePath)
    }

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
    ): Path = getPhysicalPath(imageType).resolve(filename)

    /**
     * Gets the URL path for accessing images of the given type via HTTP.
     * This is the path that should be used in API responses and web URLs.
     *
     * @param imageType The type of image
     * @return The URL path prefix for this image type
     */
    fun getUrlPath(imageType: ImageType): String = getPathConfig(imageType).urlPath

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
    ): String {
        val urlPath = getUrlPath(imageType)
        return if (urlPath.endsWith("/")) {
            "$urlPath$filename"
        } else {
            "$urlPath/$filename"
        }
    }

    /**
     * Checks if the given image type is publicly accessible via static URL mapping.
     *
     * @param imageType The type of image
     * @return true if publicly accessible, false if API-controlled access
     */
    fun isPubliclyAccessible(imageType: ImageType): Boolean = getPathConfig(imageType).isPubliclyAccessible

    /**
     * Gets all configured image types.
     *
     * @return Set of all configured ImageType values
     */
    fun getAllConfiguredImageTypes(): Set<ImageType> = storagePathConfiguration.pathMappings.keys

    /**
     * Finds the ImageType for a given filename by checking which directory contains it.
     * This is useful for operations where only the filename is known.
     *
     * @param filename The filename to search for
     * @return The ImageType if found, null otherwise
     */
    fun findImageTypeByFilename(filename: String): ImageType? =
        storagePathConfiguration.pathMappings.keys.find { imageType ->
            val filePath = getPhysicalFilePath(imageType, filename)
            Files.exists(filePath)
        }

    /**
     * Gets the relative path from storage root for the given image type.
     *
     * @param imageType The type of image
     * @return The relative path from storage root
     */
    fun getRelativePath(imageType: ImageType): String = getPathConfig(imageType).relativePath

    /**
     * Gets the storage root path.
     *
     * @return The absolute Path to the storage root directory
     */
    fun getStorageRoot(): Path = storagePathConfiguration.storageRoot

    private fun getPathConfig(imageType: ImageType) =
        storagePathConfiguration.pathMappings[imageType]
            ?: throw IllegalArgumentException("No path configuration found for ImageType: $imageType")

    private fun createDirectories() {
        storagePathConfiguration.pathMappings.forEach { (imageType, pathConfig) ->
            try {
                val physicalPath = storagePathConfiguration.storageRoot.resolve(pathConfig.relativePath)
                Files.createDirectories(physicalPath)
                logger.info("Created/verified directory for $imageType: ${physicalPath.toAbsolutePath()}")
            } catch (e: IOException) {
                logger.error("Failed to create directory for $imageType: ${e.message}", e)
                throw RuntimeException("Failed to create storage directory for $imageType: ${e.message}", e)
            }
        }
    }
}
