package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.dto.CreateImageRequest
import com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.UpdateGeneratedImageRequest
import com.jotoai.voenix.shop.image.api.dto.UploadedImageDto
import com.jotoai.voenix.shop.image.api.exceptions.ImageNotFoundException
import com.jotoai.voenix.shop.image.api.exceptions.ImageStorageException
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.util.UUID

/**
 * Main service for image operations that coordinates business logic and delegates to specialized services.
 * This service handles entity persistence and business rules while delegating
 * storage operations to ImageStorageService and generation operations to generation services.
 */
@Service
class ImageService(
    private val imageStorageService: ImageStorageService,
    private val userImageStorageService: UserImageStorageService,
    private val uploadedImageRepository: UploadedImageRepository,
    private val generatedImageRepository: GeneratedImageRepository,
) {
    private val logger = org.slf4j.LoggerFactory.getLogger(ImageService::class.java)

    /**
     * Stores an image file using the storage service. This is primarily for compatibility.
     * For user uploads, use createUploadedImage instead.
     */
    fun store(
        file: MultipartFile,
        request: CreateImageRequest,
    ): ImageDto {
        logger.debug(
            "Delegating image storage - Type: {}, Original filename: {}",
            request.imageType,
            file.originalFilename,
        )
        return imageStorageService.storeImage(file, request)
    }

    /**
     * Retrieves image data by delegating to the storage service.
     */
    fun getImageData(
        filename: String,
        imageType: ImageType,
    ): Pair<ByteArray, String> = imageStorageService.getImageData(filename, imageType)

    /**
     * Deletes an image by delegating to the storage service.
     */
    fun delete(
        filename: String,
        imageType: ImageType,
    ) = imageStorageService.deleteImage(filename, imageType)

    /**
     * Retrieves image data by filename only (searches across image types).
     */
    fun getImageData(filename: String): Pair<ByteArray, String> = imageStorageService.getImageData(filename)

    /**
     * Deletes an image by filename only (searches across image types).
     */
    fun delete(filename: String) = imageStorageService.deleteImage(filename)

    // Uploaded Image Business Logic

    @Transactional
    fun createUploadedImage(
        file: MultipartFile,
        userId: Long,
    ): UploadedImageDto {
        try {
            val uploadedImage = userImageStorageService.storeUploadedImage(file, userId)

            return UploadedImageDto(
                filename = uploadedImage.storedFilename,
                imageType = ImageType.PRIVATE,
                uuid = uploadedImage.uuid,
                originalFilename = uploadedImage.originalFilename,
                contentType = uploadedImage.contentType,
                fileSize = uploadedImage.fileSize,
                uploadedAt = uploadedImage.uploadedAt,
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

    @Transactional(readOnly = true)
    fun getUploadedImageByUuid(
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

    @Transactional
    fun deleteUploadedImage(
        uuid: UUID,
        userId: Long,
    ) {
        val uploadedImage =
            uploadedImageRepository.findByUserIdAndUuid(userId, uuid)
                ?: throw ImageNotFoundException("Uploaded image with UUID $uuid not found for user $userId")

        try {
            // Delete file from storage
            imageStorageService.deleteImage(uploadedImage.storedFilename)

            // Delete from database
            uploadedImageRepository.delete(uploadedImage)
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

    @Transactional(readOnly = true)
    fun getUserUploadedImages(userId: Long): List<UploadedImageDto> =
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

    // Generated Image Business Logic

    @Transactional(readOnly = true)
    fun getGeneratedImageByUuid(
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

    @Transactional
    fun updateGeneratedImage(
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

    @Transactional
    fun deleteGeneratedImage(
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
            imageStorageService.deleteImage(generatedImage.filename)

            // Delete from database
            generatedImageRepository.delete(generatedImage)
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

    @Transactional(readOnly = true)
    fun getUserGeneratedImages(userId: Long): List<GeneratedImageDto> =
        generatedImageRepository
            .findAllByUserIdWithUploadedImage(userId)
            .map { generatedImage ->
                GeneratedImageDto(
                    filename = generatedImage.filename,
                    imageType = ImageType.GENERATED,
                    promptId = generatedImage.promptId,
                    userId = generatedImage.userId,
                    generatedAt = generatedImage.generatedAt,
                    ipAddress = generatedImage.ipAddress,
                )
            }
}
