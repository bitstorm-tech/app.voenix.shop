package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.image.api.ImageStorageService
import com.jotoai.voenix.shop.image.api.dto.CreateImageRequest
import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.image.api.dto.UpdateGeneratedImageRequest
import com.jotoai.voenix.shop.image.api.dto.UploadedImageDto
import com.jotoai.voenix.shop.image.api.exceptions.ImageNotFoundException
import com.jotoai.voenix.shop.image.api.exceptions.ImageStorageException
import com.jotoai.voenix.shop.image.internal.orchestration.ImageGenerationOrchestrationService
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.time.LocalDateTime
import java.util.*

/**
 * Consolidated image management service that handles all image operations.
 * This service combines the functionality of ImageService, ImageFacadeImpl, and ImageAccessValidationService
 * to provide a unified interface for image management with access validation.
 *
 * Features:
 * - Implements ImageFacade interface directly
 * - Handles uploaded and generated images
 * - Includes access validation logic
 * - Manages image metadata and database operations
 * - Coordinates with storage services
 * - Provides caching for performance
 */
@Service
class ImageManagementService(
    private val imageStorageService: ImageStorageService,
    private val uploadedImageRepository: UploadedImageRepository,
    private val generatedImageRepository: GeneratedImageRepository,
    private val imageValidationService: ImageValidationService,
    private val imageGenerationOrchestrationService: ImageGenerationOrchestrationService,
) : ImageFacade {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val ORIGINAL_SUFFIX = "_original"
        private const val GENERATED_PREFIX = "_generated_"
    }

    // Implementation of ImageFacade interface

    @Transactional
    override fun createUploadedImage(
        file: MultipartFile,
        userId: Long,
    ): UploadedImageDto {
        // Delegate to the overloaded method with default PRIVATE type for backward compatibility
        return createUploadedImage(file, userId, ImageType.PRIVATE)
    }

    @Transactional
    override fun createUploadedImage(
        file: MultipartFile,
        userId: Long,
        imageType: ImageType,
    ): UploadedImageDto {
        try {
            logger.debug { "Creating uploaded image for user $userId with type $imageType" }

            val uuid = UUID.randomUUID()
            val originalFilename = file.originalFilename ?: "unknown"
            val contentType = file.contentType ?: "application/octet-stream"
            val fileSize = file.size
            val uploadedAt = LocalDateTime.now()

            // Route to appropriate storage based on imageType
            val storedFilename =
                when (imageType) {
                    ImageType.PRIVATE -> {
                        // For private images, use the existing user-specific storage
                        val storageImpl = imageStorageService as ImageStorageServiceImpl
                        val uploadedImage = storageImpl.storeUploadedImage(file, userId)
                        // Return the DTO with the actual UUID from the saved entity
                        return UploadedImageDto(
                            filename = uploadedImage.storedFilename,
                            imageType = imageType,
                            id = uploadedImage.id,
                            uuid = uploadedImage.uuid, // Use the UUID from the saved entity
                            originalFilename = uploadedImage.originalFilename,
                            contentType = uploadedImage.contentType,
                            fileSize = uploadedImage.fileSize,
                            uploadedAt = uploadedImage.uploadedAt,
                        )
                    }
                    else -> {
                        // For all other types, use the standard storage with correct path
                        imageStorageService.storeFile(file, imageType)
                    }
                }

            return UploadedImageDto(
                filename = storedFilename,
                imageType = imageType,
                uuid = uuid,
                originalFilename = originalFilename,
                contentType = contentType,
                fileSize = fileSize,
                uploadedAt = uploadedAt,
            )
        } catch (e: DataAccessException) {
            throw ImageStorageException("Failed to create uploaded image: ${e.message}", e)
        } catch (e: IOException) {
            throw ImageStorageException("Failed to create uploaded image: ${e.message}", e)
        } catch (e: IllegalStateException) {
            throw ImageStorageException("Failed to create uploaded image: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            throw ImageStorageException("Failed to create uploaded image: ${e.message}", e)
        }
    }

    override fun createImage(request: CreateImageRequest): ImageDto {
        // This method is not implemented as it's superseded by createUploadedImage which works with MultipartFile
        // The CreateImageRequest doesn't contain the actual image data needed for implementation
        throw UnsupportedOperationException(
            "This method is not supported. Use createUploadedImage() with MultipartFile for uploading images, " +
                "or use the appropriate generation methods for creating generated images.",
        )
    }

    @Cacheable("uploadedImages", key = "#uuid + '_' + #userId")
    @Transactional(readOnly = true)
    override fun getUploadedImageByUuid(
        uuid: UUID,
        userId: Long,
    ): UploadedImageDto {
        val uploadedImage =
            uploadedImageRepository.findByUserIdAndUuid(userId, uuid)
                ?: throw ImageNotFoundException("Uploaded image with UUID $uuid not found for user $userId")

        return UploadedImageDto(
            filename = uploadedImage.storedFilename,
            imageType = ImageType.PRIVATE,
            uuid = uploadedImage.uuid,
            originalFilename = uploadedImage.originalFilename,
            contentType = uploadedImage.contentType,
            fileSize = uploadedImage.fileSize,
            uploadedAt = uploadedImage.uploadedAt,
        )
    }

    @CacheEvict("uploadedImages", key = "#uuid + '_' + #userId")
    @Transactional
    override fun deleteUploadedImage(
        uuid: UUID,
        userId: Long,
    ) {
        val uploadedImage =
            uploadedImageRepository.findByUserIdAndUuid(userId, uuid)
                ?: throw ImageNotFoundException("Uploaded image with UUID $uuid not found for user $userId")

        try {
            // Delete file from storage
            val storageImpl = imageStorageService as ImageStorageServiceImpl
            storageImpl.deleteImage(uploadedImage.storedFilename)

            // Delete from database
            uploadedImageRepository.delete(uploadedImage)
            logger.debug { "Deleted uploaded image $uuid for user $userId" }
        } catch (e: DataAccessException) {
            throw ImageStorageException("Failed to delete uploaded image: ${e.message}", e)
        } catch (e: IOException) {
            throw ImageStorageException("Failed to delete uploaded image: ${e.message}", e)
        } catch (e: IllegalStateException) {
            throw ImageStorageException("Failed to delete uploaded image: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            throw ImageStorageException("Failed to delete uploaded image: ${e.message}", e)
        }
    }

    @Cacheable("userUploadedImages", key = "#userId")
    @Transactional(readOnly = true)
    override fun getUserUploadedImages(userId: Long): List<UploadedImageDto> =
        uploadedImageRepository
            .findAllByUserIdWithGeneratedImages(userId)
            .map { uploadedImage ->
                UploadedImageDto(
                    filename = uploadedImage.storedFilename,
                    imageType = ImageType.PRIVATE,
                    uuid = uploadedImage.uuid,
                    originalFilename = uploadedImage.originalFilename,
                    contentType = uploadedImage.contentType,
                    fileSize = uploadedImage.fileSize,
                    uploadedAt = uploadedImage.uploadedAt,
                )
            }

    // Generated Images Implementation

    @Cacheable("generatedImages", key = "#uuid + '_' + (#userId ?: 'null')")
    @Transactional(readOnly = true)
    override fun getGeneratedImageByUuid(
        uuid: UUID,
        userId: Long?,
    ): GeneratedImageDto {
        val generatedImage =
            if (userId != null) {
                generatedImageRepository.findByUuidAndUserId(uuid, userId)
                    ?: throw ImageNotFoundException("Generated image with UUID $uuid not found for user $userId")
            } else {
                generatedImageRepository.findByUuid(uuid)
                    ?: throw ImageNotFoundException("Generated image with UUID $uuid not found")
            }

        return GeneratedImageDto(
            filename = generatedImage.filename,
            imageType = ImageType.GENERATED,
            promptId = generatedImage.promptId,
            userId = generatedImage.userId,
            generatedAt = generatedImage.generatedAt,
            ipAddress = generatedImage.ipAddress,
        )
    }

    @CacheEvict("generatedImages", key = "#uuid + '_' + (#userId ?: 'null')")
    @Transactional
    override fun updateGeneratedImage(
        uuid: UUID,
        updateRequest: UpdateGeneratedImageRequest,
        userId: Long?,
    ): GeneratedImageDto {
        val generatedImage =
            if (userId != null) {
                generatedImageRepository.findByUuidAndUserId(uuid, userId)
                    ?: throw ImageNotFoundException("Generated image with UUID $uuid not found for user $userId")
            } else {
                generatedImageRepository.findByUuid(uuid)
                    ?: throw ImageNotFoundException("Generated image with UUID $uuid not found")
            }

        // Update allowed fields if provided
        updateRequest.promptId?.let { generatedImage.promptId = it }
        updateRequest.userId?.let { generatedImage.userId = it }
        updateRequest.ipAddress?.let { generatedImage.ipAddress = it }

        try {
            val saved = generatedImageRepository.save(generatedImage)

            return GeneratedImageDto(
                filename = saved.filename,
                imageType = ImageType.GENERATED,
                promptId = saved.promptId,
                userId = saved.userId,
                generatedAt = saved.generatedAt,
                ipAddress = saved.ipAddress,
            )
        } catch (e: DataAccessException) {
            throw ImageStorageException("Failed to update generated image: ${e.message}", e)
        } catch (e: IllegalStateException) {
            throw ImageStorageException("Failed to update generated image: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            throw ImageStorageException("Failed to update generated image: ${e.message}", e)
        }
    }

    @CacheEvict("generatedImages", key = "#uuid + '_' + (#userId ?: 'null')")
    @Transactional
    override fun deleteGeneratedImage(
        uuid: UUID,
        userId: Long?,
    ) {
        val generatedImage =
            if (userId != null) {
                generatedImageRepository.findByUuidAndUserId(uuid, userId)
                    ?: throw ImageNotFoundException("Generated image with UUID $uuid not found for user $userId")
            } else {
                generatedImageRepository.findByUuid(uuid)
                    ?: throw ImageNotFoundException("Generated image with UUID $uuid not found")
            }

        try {
            // Delete file from storage
            val storageImpl = imageStorageService as ImageStorageServiceImpl
            storageImpl.deleteImage(generatedImage.filename)

            // Delete from database
            generatedImageRepository.delete(generatedImage)
            logger.debug { "Deleted generated image $uuid${userId?.let { " for user $it" } ?: ""}" }
        } catch (e: DataAccessException) {
            throw ImageStorageException("Failed to delete generated image: ${e.message}", e)
        } catch (e: IOException) {
            throw ImageStorageException("Failed to delete generated image: ${e.message}", e)
        } catch (e: IllegalStateException) {
            throw ImageStorageException("Failed to delete generated image: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            throw ImageStorageException("Failed to delete generated image: ${e.message}", e)
        }
    }

    @Cacheable("userGeneratedImages", key = "#userId")
    @Transactional(readOnly = true)
    override fun getUserGeneratedImages(userId: Long): List<GeneratedImageDto> =
        generatedImageRepository
            .findAllByUserIdWithUploadedImage(userId)
            .map { generatedImage ->
                GeneratedImageDto(
                    filename = generatedImage.filename,
                    imageType = ImageType.GENERATED,
                    id = generatedImage.id,
                    promptId = generatedImage.promptId,
                    userId = generatedImage.userId,
                    generatedAt = generatedImage.generatedAt,
                    ipAddress = generatedImage.ipAddress,
                )
            }

    @Transactional
    override fun storeGeneratedImage(
        imageBytes: ByteArray,
        uploadedImageId: Long,
        promptId: Long,
        generationNumber: Int,
    ): GeneratedImageDto {
        try {
            logger.info { "Storing generated image for uploaded image ID: $uploadedImageId" }

            // Get the uploaded image to get the user ID
            val uploadedImage =
                uploadedImageRepository.findById(uploadedImageId).orElseThrow {
                    ResourceNotFoundException("Uploaded image with ID $uploadedImageId not found")
                }

            val storageImpl = imageStorageService as ImageStorageServiceImpl
            val generatedImage =
                storageImpl.storeGeneratedImage(
                    imageBytes = imageBytes,
                    uploadedImage = uploadedImage,
                    promptId = promptId,
                    generationNumber = generationNumber,
                )

            logger.info { "Successfully stored generated image: ${generatedImage.filename}" }

            return GeneratedImageDto(
                filename = generatedImage.filename,
                imageType = ImageType.GENERATED,
                id = generatedImage.id,
                promptId = generatedImage.promptId,
                userId = generatedImage.userId,
                generatedAt = generatedImage.generatedAt,
                ipAddress = generatedImage.ipAddress,
            )
        } catch (e: DataAccessException) {
            logger.error(e) { "Database error storing generated image" }
            throw ImageStorageException("Failed to store generated image: ${e.message}", e)
        } catch (e: IOException) {
            logger.error(e) { "I/O error storing generated image" }
            throw ImageStorageException("Failed to store generated image: ${e.message}", e)
        } catch (e: IllegalStateException) {
            logger.error(e) { "State error storing generated image" }
            throw ImageStorageException("Failed to store generated image: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Argument error storing generated image" }
            throw ImageStorageException("Failed to store generated image: ${e.message}", e)
        }
    }

    @Transactional(readOnly = true)
    override fun validateImageFile(file: MultipartFile) {
        imageValidationService.validateImageFile(file)
    }

    @Transactional(readOnly = true)
    override fun countGeneratedImagesForIpAfter(
        ipAddress: String,
        after: LocalDateTime,
    ): Long = generatedImageRepository.countByIpAddressAndGeneratedAtAfter(ipAddress, after)

    @Transactional(readOnly = true)
    override fun countGeneratedImagesForUserAfter(
        userId: Long,
        after: LocalDateTime,
    ): Long = generatedImageRepository.countByUserIdAndGeneratedAtAfter(userId, after)

    @Transactional
    override fun storePublicGeneratedImage(
        imageBytes: ByteArray,
        promptId: Long,
        ipAddress: String,
        generationNumber: Int,
    ): GeneratedImageDto {
        try {
            logger.info { "Storing public generated image for prompt ID: $promptId" }

            // Generate filename for this image
            val filename = "${UUID.randomUUID()}_generated_$generationNumber.png"

            // Store the image bytes
            imageStorageService.storeFile(imageBytes, filename, ImageType.PUBLIC)

            // Create and save the database record
            val generatedImage =
                com.jotoai.voenix.shop.image.internal.entity.GeneratedImage(
                    filename = filename,
                    promptId = promptId,
                    userId = null, // Anonymous user
                    ipAddress = ipAddress,
                    generatedAt = LocalDateTime.now(),
                )
            val savedImage = generatedImageRepository.save(generatedImage)

            logger.info { "Successfully stored public generated image: $filename" }

            return GeneratedImageDto(
                filename = savedImage.filename,
                imageType = ImageType.GENERATED,
                id = savedImage.id,
                promptId = savedImage.promptId,
                userId = savedImage.userId,
                generatedAt = savedImage.generatedAt,
                ipAddress = savedImage.ipAddress,
            )
        } catch (e: DataAccessException) {
            logger.error(e) { "Database error storing public generated image" }
            throw ImageStorageException("Failed to store public generated image: ${e.message}", e)
        } catch (e: IOException) {
            logger.error(e) { "I/O error storing public generated image" }
            throw ImageStorageException("Failed to store public generated image: ${e.message}", e)
        } catch (e: IllegalStateException) {
            logger.error(e) { "State error storing public generated image" }
            throw ImageStorageException("Failed to store public generated image: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Argument error storing public generated image" }
            throw ImageStorageException("Failed to store public generated image: ${e.message}", e)
        }
    }

    @Transactional
    override fun generateUserImageWithIds(
        promptId: Long,
        uploadedImageUuid: UUID,
        userId: Long,
        cropArea: CropArea?,
    ): PublicImageGenerationResponse =
        imageGenerationOrchestrationService.generateUserImageWithIds(promptId, uploadedImageUuid, userId, cropArea)

    // Additional methods for access validation (from ImageAccessValidationService)

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
        logger.debug { "Validating access to image $filename for user $userId" }

        // Check if this is an original image or generated image based on filename pattern
        val isOriginalImage = filename.contains(ORIGINAL_SUFFIX)
        val isGeneratedImage = filename.contains(GENERATED_PREFIX)

        when {
            isOriginalImage -> {
                // Extract UUID from filename (format: {uuid}_original.{ext})
                val uuid = extractUuidFromOriginalFilename(filename)
                uploadedImageRepository.findByUserIdAndUuid(userId, uuid)
                    ?: throw ResourceNotFoundException("Uploaded image not found or access denied")

                logger.debug { "Access granted to original image $filename for user $userId" }
            }
            isGeneratedImage -> {
                // For generated images, check ownership through the generated_images table
                val generatedImage =
                    generatedImageRepository.findByFilename(filename)
                        ?: throw ResourceNotFoundException("Generated image not found")

                if (generatedImage.userId != userId) {
                    throw ResourceNotFoundException("Generated image not found or access denied")
                }

                logger.debug { "Access granted to generated image $filename for user $userId" }
            }
            else -> {
                throw ResourceNotFoundException("Invalid image filename format")
            }
        }

        // If validation passes, get the image data through the storage service
        val storageImpl = imageStorageService as ImageStorageServiceImpl
        return storageImpl.getUserImageData(filename, userId)
    }

    /**
     * Returns raw image data for internal use or public access.
     */
    fun getImageData(
        filename: String,
        userId: Long? = null,
    ): Pair<ByteArray, String> =
        when {
            userId != null -> validateAccessAndGetImageData(filename, userId)
            else -> {
                val storageImpl = imageStorageService as ImageStorageServiceImpl
                storageImpl.getImageData(filename)
            }
        }

    /**
     * Returns raw image data by filename and image type.
     */
    fun getImageData(
        filename: String,
        imageType: ImageType,
    ): Pair<ByteArray, String> {
        val storageImpl = imageStorageService as ImageStorageServiceImpl
        return storageImpl.getImageData(filename, imageType)
    }

    /**
     * Deletes an image by delegating to the storage service.
     */
    fun delete(
        filename: String,
        imageType: ImageType,
    ) {
        val storageImpl = imageStorageService as ImageStorageServiceImpl
        storageImpl.deleteImage(filename, imageType)
    }

    /**
     * Deletes an image by filename only (searches across image types).
     */
    fun delete(filename: String) {
        val storageImpl = imageStorageService as ImageStorageServiceImpl
        storageImpl.deleteImage(filename)
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
            logger.error(e) { "Invalid UUID format in filename: $filename" }
            throw ResourceNotFoundException("Invalid image filename format")
        }
    }
}
