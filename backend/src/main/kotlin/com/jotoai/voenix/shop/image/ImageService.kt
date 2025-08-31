package com.jotoai.voenix.shop.image
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.UUID

/**
 * Unified public API for the Image module.
 *
 * This interface consolidates all image-related operations used by other modules,
 * providing a simplified and consistent API for image operations.
 *
 * Replaces the previous interfaces:
 * - ImageOperations (removed)
 * - ImageQueryService
 * - ImageStorage
 * - StoragePathService (fully replaced)
 */
interface ImageService {
    /**
     * Stores image data with associated metadata.
     * Handles both file uploads and programmatic byte storage.
     *
     * @param data The image data to store
     * @param metadata Associated metadata for the image
     * @return DTO containing stored image information
     */
    fun store(
        data: ImageData,
        metadata: ImageMetadata,
    ): ImageInfo

    /**
     * Retrieves multiple images by their IDs.
     * Performs batch loading to avoid N+1 query problems.
     *
     * @param ids List of image IDs to retrieve
     * @return Map of ID to ImageInfo for found images
     */
    fun find(ids: List<Long>): Map<Long, ImageInfo>

    /**
     * Counts generated images based on filter criteria.
     * Used for rate limiting and quota enforcement.
     *
     * @param filter Filter criteria for counting
     * @return Number of images matching the filter
     */
    fun count(filter: CountFilter): Long

    /**
     * Retrieves image content by filename.
     * Includes access control based on user ownership.
     *
     * @param filename The filename to retrieve
     * @param userId Optional user ID for access control
     * @return Image content with bytes and metadata
     */
    fun get(
        filename: String,
        userId: Long? = null,
    ): ImageContent

    /**
     * Gets the complete URL for accessing a specific image file.
     *
     * @param filename The filename
     * @param type The image type
     * @return Complete URL path to access the image
     */
    fun getUrl(
        filename: String,
        type: ImageType,
    ): String

    /**
     * Deletes an image file from storage.
     *
     * @param filename The filename to delete
     * @param type The image type for location resolution
     * @return true if deletion was successful
     */
    fun delete(
        filename: String,
        type: ImageType,
    ): Boolean

    /**
     * Validates image data or operations.
     * TODO: validation should not be done by other packages, this should be internal
     *
     * @param validation The validation request to perform
     * @return Validation result with success status and optional message
     */
    fun validate(validation: ValidationRequest): ValidationResult

    /**
     * Retrieves an uploaded image by its UUID.
     * Legacy method for backward compatibility.
     *
     * @param uuid The unique identifier of the uploaded image
     * @param userId The ID of the user requesting the image
     * @return DTO containing uploaded image metadata
     */
    fun getUploadedImageByUuid(
        uuid: UUID,
        userId: Long,
    ): ImageInfo

    /**
     * Serves a user's image as an HTTP response.
     * Legacy method for backward compatibility with controllers.
     *
     * @param filename The filename to serve
     * @param userId The ID of the user requesting the image
     * @return ResponseEntity with the image resource
     */
    fun serveUserImage(
        filename: String,
        userId: Long,
    ): ResponseEntity<Resource>
}

/**
 * Represents different types of image data that can be stored.
 */
sealed class ImageData {
    /**
     * Image data from a multipart file upload.
     */
    data class File(
        val file: MultipartFile,
        val cropArea: CropArea? = null,
    ) : ImageData()

    /**
     * Image data from raw bytes.
     */
    data class Bytes(
        val bytes: ByteArray,
        val filename: String,
    ) : ImageData() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Bytes

            if (!bytes.contentEquals(other.bytes)) return false
            if (filename != other.filename) return false

            return true
        }

        override fun hashCode(): Int {
            var result = bytes.contentHashCode()
            result = 31 * result + filename.hashCode()
            return result
        }
    }
}

/**
 * Metadata associated with image storage operations.
 */
data class ImageMetadata(
    val type: ImageType,
    val userId: Long? = null,
    val promptId: Long? = null,
    val ipAddress: String? = null,
    val uploadedImageId: Long? = null,
    val generationNumber: Int? = null,
)

/**
 * Filter criteria for counting generated images.
 */
data class CountFilter(
    val userId: Long? = null,
    val ipAddress: String? = null,
    val after: LocalDateTime,
)

/**
 * Image content with raw bytes and metadata.
 */
data class ImageContent(
    val bytes: ByteArray,
    val contentType: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageContent

        if (!bytes.contentEquals(other.bytes)) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + contentType.hashCode()
        return result
    }
}

/**
 * Different types of validation requests.
 */
sealed class ValidationRequest {
    /**
     * Validates a file upload before processing.
     */
    data class FileUpload(
        val file: MultipartFile,
    ) : ValidationRequest()

    /**
     * Validates image ownership for operations.
     */
    data class Ownership(
        val imageId: Long,
        val userId: Long?,
    ) : ValidationRequest()
}

/**
 * Result of a validation operation.
 */
data class ValidationResult(
    val valid: Boolean,
    val message: String? = null,
)
