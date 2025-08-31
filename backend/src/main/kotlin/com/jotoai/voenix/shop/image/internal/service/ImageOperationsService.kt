package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.application.api.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.ImageOperations
import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.UpdateGeneratedImageRequest
import com.jotoai.voenix.shop.image.api.dto.UploadedImageDto
import com.jotoai.voenix.shop.image.api.exceptions.ImageNotFoundException
import com.jotoai.voenix.shop.image.api.exceptions.ImageStorageException
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.time.LocalDateTime
import java.util.UUID

@Service
@Transactional
class ImageOperationsService(
    private val fileStorageService: FileStorageService,
    private val uploadedImageRepository: UploadedImageRepository,
    private val generatedImageRepository: GeneratedImageRepository,
    private val imageValidationService: ImageValidationService,
    private val userImageStorageService: UserImageStorageService,
) : ImageOperations {
    
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun createUploadedImage(
        file: MultipartFile,
        userId: Long,
        imageType: ImageType,
        cropArea: CropArea?,
    ): UploadedImageDto {
        try {
            logger.debug { "Creating uploaded image for user $userId with type $imageType" }

            val uuid = UUID.randomUUID()
            val originalFilename = file.originalFilename ?: "unknown"
            val contentType = file.contentType ?: "application/octet-stream"
            val fileSize = file.size
            val uploadedAt = LocalDateTime.now()

            // Route to appropriate storage based on imageType
            val storedFilename = when (imageType) {
                ImageType.PRIVATE -> {
                    // For private images, use the user-specific storage service
                    val uploadedImage = userImageStorageService.storeUploadedImage(file, userId, cropArea)
                    // Return the DTO with the actual UUID from the saved entity
                    return UploadedImageDto(
                        filename = uploadedImage.storedFilename,
                        imageType = imageType,
                        id = uploadedImage.id,
                        uuid = uploadedImage.uuid,
                        originalFilename = uploadedImage.originalFilename,
                        contentType = uploadedImage.contentType,
                        fileSize = uploadedImage.fileSize,
                        uploadedAt = uploadedImage.uploadedAt,
                    )
                }
                else -> {
                    // For all other types, use the standard storage with correct path
                    fileStorageService.storeFile(file, imageType, cropArea)
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

    @Transactional(readOnly = true)
    override fun getUploadedImageByUuid(uuid: UUID, userId: Long): UploadedImageDto {
        val uploadedImage = uploadedImageRepository.findByUserIdAndUuid(userId, uuid)
            ?: throw ImageNotFoundException("Uploaded image with UUID $uuid not found for user $userId")

        return UploadedImageDto(
            filename = uploadedImage.storedFilename,
            imageType = ImageType.PRIVATE,
            id = uploadedImage.id,
            uuid = uploadedImage.uuid,
            originalFilename = uploadedImage.originalFilename,
            contentType = uploadedImage.contentType,
            fileSize = uploadedImage.fileSize,
            uploadedAt = uploadedImage.uploadedAt,
        )
    }

    @Transactional(readOnly = true)
    override fun getUserUploadedImages(userId: Long): List<UploadedImageDto> =
        uploadedImageRepository.findAllByUserIdWithGeneratedImages(userId).map { uploadedImage ->
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

    override fun storeGeneratedImage(
        imageBytes: ByteArray,
        uploadedImageId: Long,
        promptId: Long,
        generationNumber: Int,
        ipAddress: String?,
    ): GeneratedImageDto {
        try {
            logger.info { "Storing generated image for uploaded image ID: $uploadedImageId" }

            val uploadedImage = uploadedImageRepository.findById(uploadedImageId).orElseThrow {
                ResourceNotFoundException("Uploaded image with ID $uploadedImageId not found")
            }

            val generatedImage = userImageStorageService.storeGeneratedImage(
                imageBytes = imageBytes,
                uploadedImage = uploadedImage,
                promptId = promptId,
                generationNumber = generationNumber,
                ipAddress = ipAddress,
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

    override fun storePublicGeneratedImage(
        imageBytes: ByteArray,
        promptId: Long,
        ipAddress: String,
        generationNumber: Int,
    ): GeneratedImageDto {
        try {
            logger.info { "Storing public generated image for prompt ID: $promptId" }

            val filename = "${UUID.randomUUID()}_generated_$generationNumber.png"
            fileStorageService.storeFile(imageBytes, filename, ImageType.PUBLIC)

            val generatedImage = com.jotoai.voenix.shop.image.internal.entity.GeneratedImage(
                filename = filename,
                promptId = promptId,
                userId = null,
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

    @Transactional(readOnly = true)
    override fun getGeneratedImageByUuid(uuid: UUID, userId: Long?): GeneratedImageDto {
        val generatedImage = if (userId != null) {
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

    override fun updateGeneratedImage(
        uuid: UUID,
        updateRequest: UpdateGeneratedImageRequest,
        userId: Long?,
    ): GeneratedImageDto {
        val generatedImage = if (userId != null) {
            generatedImageRepository.findByUuidAndUserId(uuid, userId)
                ?: throw ImageNotFoundException("Generated image with UUID $uuid not found for user $userId")
        } else {
            generatedImageRepository.findByUuid(uuid)
                ?: throw ImageNotFoundException("Generated image with UUID $uuid not found")
        }

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

    override fun deleteGeneratedImage(uuid: UUID, userId: Long?) {
        val generatedImage = if (userId != null) {
            generatedImageRepository.findByUuidAndUserId(uuid, userId)
                ?: throw ImageNotFoundException("Generated image with UUID $uuid not found for user $userId")
        } else {
            generatedImageRepository.findByUuid(uuid)
                ?: throw ImageNotFoundException("Generated image with UUID $uuid not found")
        }

        try {
            if (generatedImage.userId != null) {
                userImageStorageService.deleteUserImage(generatedImage.filename, generatedImage.userId!!)
            } else {
                fileStorageService.deleteFile(generatedImage.filename, ImageType.PUBLIC)
            }

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

    @Transactional(readOnly = true)
    override fun getUserGeneratedImages(userId: Long): List<GeneratedImageDto> =
        generatedImageRepository.findAllByUserIdWithUploadedImage(userId).map { generatedImage ->
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

    @Transactional(readOnly = true)
    override fun countGeneratedImagesForIpAfter(ipAddress: String, after: LocalDateTime): Long =
        generatedImageRepository.countByIpAddressAndGeneratedAtAfter(ipAddress, after)

    @Transactional(readOnly = true)
    override fun countGeneratedImagesForUserAfter(userId: Long, after: LocalDateTime): Long =
        generatedImageRepository.countByUserIdAndGeneratedAtAfter(userId, after)

    @Transactional(readOnly = true)
    override fun validateImageFile(file: MultipartFile) {
        imageValidationService.validateImageFile(file)
    }
}
