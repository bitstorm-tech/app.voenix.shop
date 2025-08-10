package com.jotoai.voenix.shop.image.internal.service

import org.slf4j.LoggerFactory
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * Abstract base class providing common storage operations for file handling.
 * Contains shared logic for file validation, directory operations, and basic I/O.
 */
abstract class BaseStorageService {
    protected val logger = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val BYTES_PER_KB = 1024
        private const val BYTES_PER_MB = BYTES_PER_KB * BYTES_PER_KB
    }

    /**
     * Validates a MultipartFile for basic requirements.
     * @param file The file to validate
     * @param maxFileSize Maximum allowed file size in bytes
     * @param allowedContentTypes Set of allowed content types (e.g., "image/jpeg")
     * @throws IllegalArgumentException if validation fails
     */
    protected fun validateFile(
        file: MultipartFile,
        maxFileSize: Long,
        allowedContentTypes: Set<String>,
    ) {
        if (file.isEmpty) {
            throw IllegalArgumentException("Cannot upload empty file")
        }

        if (file.size > maxFileSize) {
            val maxSizeMB = maxFileSize / BYTES_PER_MB
            throw IllegalArgumentException("File size exceeds maximum allowed size of ${maxSizeMB}MB")
        }

        val contentType = file.contentType?.lowercase()
        if (contentType == null || contentType !in allowedContentTypes) {
            val allowedFormats = allowedContentTypes.joinToString(", ")
            throw IllegalArgumentException("Invalid file format. Allowed formats: $allowedFormats")
        }
    }

    /**
     * Creates directories if they don't exist.
     * @param directoryPath The path to create
     * @throws RuntimeException if directory creation fails
     */
    protected fun ensureDirectoryExists(directoryPath: Path) {
        try {
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath)
                logger.debug("Created directory: ${directoryPath.toAbsolutePath()}")
            }
        } catch (e: IOException) {
            logger.error("Failed to create directory ${directoryPath.toAbsolutePath()}: ${e.message}", e)
            throw RuntimeException("Failed to create directory: ${e.message}", e)
        }
    }

    /**
     * Writes bytes to a file path.
     * @param filePath The target file path
     * @param bytes The bytes to write
     * @throws RuntimeException if writing fails
     */
    protected fun writeFile(
        filePath: Path,
        bytes: ByteArray,
    ) {
        try {
            Files.write(filePath, bytes)
            logger.debug("Successfully wrote file: ${filePath.toAbsolutePath()}")
        } catch (e: IOException) {
            logger.error("Failed to write file at ${filePath.toAbsolutePath()}: ${e.message}", e)
            throw RuntimeException("Failed to write file: ${e.message}", e)
        }
    }

    /**
     * Reads all bytes from a file.
     * @param filePath The file path to read
     * @return The file bytes
     * @throws RuntimeException if reading fails
     */
    protected fun readFile(filePath: Path): ByteArray {
        try {
            return Files.readAllBytes(filePath)
        } catch (e: IOException) {
            logger.error("Failed to read file ${filePath.toAbsolutePath()}: ${e.message}", e)
            throw RuntimeException("Failed to read file: ${e.message}", e)
        }
    }

    /**
     * Checks if a file exists at the given path.
     * @param filePath The file path to check
     * @return true if file exists, false otherwise
     */
    protected fun fileExists(filePath: Path): Boolean = Files.exists(filePath)

    /**
     * Deletes a file if it exists.
     * @param filePath The file path to delete
     * @return true if file was deleted, false if it didn't exist
     * @throws RuntimeException if deletion fails
     */
    protected fun deleteFile(filePath: Path): Boolean {
        try {
            return Files.deleteIfExists(filePath)
        } catch (e: IOException) {
            logger.error("Failed to delete file ${filePath.toAbsolutePath()}: ${e.message}", e)
            throw RuntimeException("Failed to delete file: ${e.message}", e)
        }
    }

    /**
     * Probes the content type of a file.
     * @param filePath The file path to probe
     * @param defaultContentType Default content type if probing fails
     * @return The detected or default content type
     */
    protected fun probeContentType(
        filePath: Path,
        defaultContentType: String = "application/octet-stream",
    ): String =
        try {
            Files.probeContentType(filePath) ?: defaultContentType
        } catch (e: IOException) {
            logger.warn(
                "Failed to probe content type for ${filePath.toAbsolutePath()}, " +
                    "using default: $defaultContentType",
            )
            defaultContentType
        }

    /**
     * Extracts file extension from filename.
     * @param filename The filename to extract extension from
     * @return The file extension including the dot (e.g., ".jpg"), or empty string if no extension
     */
    protected fun getFileExtension(filename: String): String {
        val lastDotIndex = filename.lastIndexOf('.')
        return if (lastDotIndex > 0) filename.substring(lastDotIndex) else ""
    }
}
