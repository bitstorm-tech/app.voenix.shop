package com.jotoai.voenix.shop.domain.images.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.images.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.domain.images.repository.UploadedImageRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Service responsible for validating user access to images and ensuring security.
 * Handles both uploaded images and generated images with proper ownership validation.
 */
@Service
@Transactional(readOnly = true)
class ImageAccessService(
    private val uploadedImageRepository: UploadedImageRepository,
    private val generatedImageRepository: GeneratedImageRepository,
    private val userImageStorageService: UserImageStorageService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ImageAccessService::class.java)
        private const val ORIGINAL_SUFFIX = "_original"
        private const val GENERATED_PREFIX = "_generated_"
    }

    /**
     * Validates that a user has access to a specific image file and returns the image data.
     * @param filename The image filename
     * @param userId The user ID requesting access
     * @return Pair of image bytes and content type
     * @throws ResourceNotFoundException if image not found or access denied
     */
    fun validateAccessAndGetImageData(
        filename: String,
        userId: Long,
    ): Pair<ByteArray, String> {
        logger.debug("Validating access to image $filename for user $userId")

        // Check if this is an original image or generated image based on filename pattern
        val isOriginalImage = filename.contains(ORIGINAL_SUFFIX)
        val isGeneratedImage = filename.contains(GENERATED_PREFIX)

        when {
            isOriginalImage -> {
                // Extract UUID from filename (format: {uuid}_original.{ext})
                val uuid = extractUuidFromOriginalFilename(filename)
                val uploadedImage =
                    uploadedImageRepository.findByUserIdAndUuid(userId, uuid)
                        ?: throw ResourceNotFoundException("Uploaded image not found or access denied")

                logger.debug("Access granted to original image $filename for user $userId")
            }
            isGeneratedImage -> {
                // For generated images, check ownership through the generated_images table
                val generatedImage =
                    generatedImageRepository.findByFilename(filename)
                        ?: throw ResourceNotFoundException("Generated image not found")

                if (generatedImage.userId != userId) {
                    throw ResourceNotFoundException("Generated image not found or access denied")
                }

                logger.debug("Access granted to generated image $filename for user $userId")
            }
            else -> {
                throw ResourceNotFoundException("Invalid image filename format")
            }
        }

        // If validation passes, get the image data through the storage service
        return userImageStorageService.getUserImageData(filename, userId)
    }

    /**
     * Validates that a user owns an uploaded image by UUID.
     * @param uuid The image UUID
     * @param userId The user ID
     * @return true if user owns the image, false otherwise
     */
    fun validateUploadedImageOwnership(
        uuid: UUID,
        userId: Long,
    ): Boolean = uploadedImageRepository.findByUserIdAndUuid(userId, uuid) != null

    /**
     * Validates that a user owns a generated image by filename.
     * @param filename The generated image filename
     * @param userId The user ID
     * @return true if user owns the image, false otherwise
     */
    fun validateGeneratedImageOwnership(
        filename: String,
        userId: Long,
    ): Boolean {
        val generatedImage = generatedImageRepository.findByFilename(filename)
        return generatedImage?.userId == userId
    }

    /**
     * Extracts UUID from original image filename.
     * Expected format: {uuid}_original.{ext}
     */
    private fun extractUuidFromOriginalFilename(filename: String): UUID {
        try {
            val uuidString = filename.substringBefore(ORIGINAL_SUFFIX)
            return UUID.fromString(uuidString)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid UUID format in filename: $filename", e)
            throw ResourceNotFoundException("Invalid image filename format")
        }
    }
}
