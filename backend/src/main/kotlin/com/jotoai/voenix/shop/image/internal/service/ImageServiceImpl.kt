package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.CountFilter
import com.jotoai.voenix.shop.image.api.ImageContent
import com.jotoai.voenix.shop.image.api.ImageData
import com.jotoai.voenix.shop.image.api.ImageMetadata
import com.jotoai.voenix.shop.image.api.ImageService
import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.image.api.ValidationRequest
import com.jotoai.voenix.shop.image.api.ValidationResult
import com.jotoai.voenix.shop.image.api.dto.ImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.UploadedImageDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * Unified implementation of ImageService that delegates to existing internal services.
 * This provides the consolidated API while maintaining the existing internal architecture.
 */
@Service
@Transactional
class ImageServiceImpl(
    private val imageOperationsService: ImageOperationsService,
    private val imageQueryServiceImpl: ImageQueryServiceImpl,
    private val fileStorageService: FileStorageService,
    private val storagePathService: StoragePathService,
) : ImageService {
    override fun store(
        data: ImageData,
        metadata: ImageMetadata,
    ): ImageDto {
        logger.debug { "Storing image with type: ${metadata.type}" }

        return when (data) {
            is ImageData.File -> storeFromFile(data, metadata)
            is ImageData.Bytes -> storeFromBytes(data, metadata)
        }
    }

    override fun find(ids: List<Long>): Map<Long, ImageDto> {
        logger.debug { "Finding images by IDs: $ids" }

        // Cast the GeneratedImageDto to ImageDto since GeneratedImageDto extends ImageDto
        return imageQueryServiceImpl
            .findGeneratedImagesByIds(ids)
            .mapValues { it.value as ImageDto }
    }

    override fun count(filter: CountFilter): Long {
        logger.debug { "Counting images with filter: $filter" }

        return when {
            filter.userId != null -> {
                imageOperationsService.countGeneratedImagesForUserAfter(filter.userId, filter.after)
            }
            filter.ipAddress != null -> {
                imageOperationsService.countGeneratedImagesForIpAfter(filter.ipAddress, filter.after)
            }
            else -> {
                logger.warn { "CountFilter requires either userId or ipAddress" }
                0L
            }
        }
    }

    override fun get(
        filename: String,
        userId: Long?,
    ): ImageContent {
        logger.debug { "Getting image content for filename: $filename, userId: $userId" }

        val (bytes, contentType) = fileStorageService.getImageData(filename, userId)
        return ImageContent(bytes, contentType)
    }

    override fun getUrl(
        filename: String,
        type: ImageType,
    ): String {
        logger.debug { "Getting URL for filename: $filename, type: $type" }

        return storagePathService.getImageUrl(type, filename)
    }

    override fun delete(
        filename: String,
        type: ImageType,
    ): Boolean {
        logger.debug { "Deleting image: $filename, type: $type" }

        return fileStorageService.deleteFile(filename, type)
    }

    override fun validate(validation: ValidationRequest): ValidationResult {
        logger.debug { "Validating request: $validation" }

        return try {
            when (validation) {
                is ValidationRequest.FileUpload -> {
                    imageOperationsService.validateImageFile(validation.file)
                    ValidationResult(valid = true)
                }
                is ValidationRequest.Ownership -> {
                    val isValid =
                        imageQueryServiceImpl.validateGeneratedImageOwnership(
                            validation.imageId,
                            validation.userId,
                        )
                    ValidationResult(
                        valid = isValid,
                        message = if (!isValid) "Image ownership validation failed" else null,
                    )
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Validation failed: ${e.message}" }
            ValidationResult(valid = false, message = e.message)
        }
    }

    // Legacy compatibility methods
    override fun getUploadedImageByUuid(
        uuid: UUID,
        userId: Long,
    ): ImageDto {
        logger.debug { "Getting uploaded image by UUID: $uuid, userId: $userId" }

        return imageOperationsService.getUploadedImageByUuid(uuid, userId)
    }

    override fun serveUserImage(
        filename: String,
        userId: Long,
    ): ResponseEntity<Resource> {
        logger.debug { "Serving user image: $filename, userId: $userId" }

        return fileStorageService.serveUserImage(filename, userId)
    }

    private fun storeFromFile(
        data: ImageData.File,
        metadata: ImageMetadata,
    ): ImageDto =
        when {
            // Handle generation use cases (OpenAI module)
            metadata.promptId != null && metadata.uploadedImageId != null -> {
                // This is storing a generated image for authenticated users
                val imageBytes = data.file.bytes
                imageOperationsService.storeGeneratedImage(
                    imageBytes = imageBytes,
                    uploadedImageId = metadata.uploadedImageId,
                    promptId = metadata.promptId,
                    generationNumber = metadata.generationNumber ?: 1,
                    ipAddress = metadata.ipAddress,
                )
            }
            metadata.promptId != null && metadata.userId == null -> {
                // This is storing a generated image for anonymous users
                val imageBytes = data.file.bytes
                imageOperationsService.storePublicGeneratedImage(
                    imageBytes = imageBytes,
                    promptId = metadata.promptId,
                    ipAddress = metadata.ipAddress ?: "unknown",
                    generationNumber = metadata.generationNumber ?: 1,
                )
            }
            metadata.userId != null -> {
                // This is a regular user upload
                imageOperationsService.createUploadedImage(
                    file = data.file,
                    userId = metadata.userId,
                    imageType = metadata.type,
                    cropArea = data.cropArea,
                )
            }
            else -> {
                // This is a generic file storage (admin uploads, variant images, etc.)
                val filename =
                    fileStorageService.storeFile(
                        file = data.file,
                        imageType = metadata.type,
                        cropArea = data.cropArea,
                    )

                // Create a simple DTO for non-user uploads
                createSimpleImageDto(filename, metadata.type)
            }
        }

    private fun storeFromBytes(
        data: ImageData.Bytes,
        metadata: ImageMetadata,
    ): ImageDto =
        when {
            // Handle generation use cases (OpenAI module)
            metadata.promptId != null && metadata.uploadedImageId != null -> {
                imageOperationsService.storeGeneratedImage(
                    imageBytes = data.bytes,
                    uploadedImageId = metadata.uploadedImageId,
                    promptId = metadata.promptId,
                    generationNumber = metadata.generationNumber ?: 1,
                    ipAddress = metadata.ipAddress,
                )
            }
            metadata.promptId != null && metadata.userId == null -> {
                imageOperationsService.storePublicGeneratedImage(
                    imageBytes = data.bytes,
                    promptId = metadata.promptId,
                    ipAddress = metadata.ipAddress ?: "unknown",
                    generationNumber = metadata.generationNumber ?: 1,
                )
            }
            else -> {
                // This is a generic file storage from bytes
                val filename =
                    fileStorageService.storeFile(
                        bytes = data.bytes,
                        originalFilename = data.filename,
                        imageType = metadata.type,
                    )

                // Create a simple DTO for byte storage
                createSimpleImageDto(filename, metadata.type)
            }
        }

    private fun createSimpleImageDto(
        filename: String,
        imageType: ImageType,
    ): ImageDto {
        // Return UploadedImageDto as it extends ImageDto and provides the required interface
        return UploadedImageDto(
            filename = filename,
            imageType = imageType,
            id = null,
            uuid = UUID.randomUUID(), // Generate a UUID for compatibility
            originalFilename = filename,
            contentType = "application/octet-stream",
            fileSize = 0L,
            uploadedAt = java.time.LocalDateTime.now(),
        )
    }
}
