package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.image.api.dto.CreateImageRequest
import com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.UploadedImageDto
import com.jotoai.voenix.shop.image.api.exceptions.ImageNotFoundException
import com.jotoai.voenix.shop.image.api.exceptions.ImageStorageException
import com.jotoai.voenix.shop.image.events.ImageCreatedEvent
import com.jotoai.voenix.shop.image.events.ImageDeletedEvent
import com.jotoai.voenix.shop.image.events.ImageUpdatedEvent
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/**
 * Implementation of ImageFacade that delegates to internal services.
 */
@Service
class ImageFacadeImpl(
    private val imageService: ImageService,
    private val userImageStorageService: UserImageStorageService,
    private val uploadedImageRepository: UploadedImageRepository,
    private val generatedImageRepository: GeneratedImageRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) : ImageFacade {
    @Transactional
    override fun createUploadedImage(
        file: MultipartFile,
        userId: Long,
    ): UploadedImageDto {
        try {
            val uploadedImage = userImageStorageService.storeUploadedImage(file, userId)

            // Publish event
            applicationEventPublisher.publishEvent(
                ImageCreatedEvent(
                    imageId = uploadedImage.id ?: throw ImageStorageException("Image ID not generated"),
                    filename = uploadedImage.storedFilename,
                    uuid = uploadedImage.uuid,
                    userId = userId,
                ),
            )

            return UploadedImageDto(
                filename = uploadedImage.storedFilename,
                imageType = ImageType.PRIVATE,
                uuid = uploadedImage.uuid,
                originalFilename = uploadedImage.originalFilename,
                contentType = uploadedImage.contentType,
                fileSize = uploadedImage.fileSize,
                uploadedAt = uploadedImage.uploadedAt,
            )
        } catch (e: Exception) {
            throw ImageStorageException("Failed to create uploaded image: ${e.message}", e)
        }
    }

    override fun createImage(request: CreateImageRequest): ImageDto =
        throw UnsupportedOperationException("Use createUploadedImage method with MultipartFile")

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
            imageService.delete(uploadedImage.storedFilename)

            // Delete from database
            uploadedImageRepository.delete(uploadedImage)

            // Publish event
            applicationEventPublisher.publishEvent(
                ImageDeletedEvent(
                    imageId = uploadedImage.id!!,
                    filename = uploadedImage.storedFilename,
                    uuid = uploadedImage.uuid,
                    userId = userId,
                ),
            )
        } catch (e: Exception) {
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
        updateData: Map<String, Any>,
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

        // Update allowed fields
        updateData["promptId"]?.let {
            if (it is Long) generatedImage.promptId = it
        }
        updateData["userId"]?.let {
            if (it is Long?) generatedImage.userId = it
        }
        updateData["ipAddress"]?.let {
            if (it is String?) generatedImage.ipAddress = it
        }

        try {
            val saved = generatedImageRepository.save(generatedImage)

            // Publish event
            applicationEventPublisher.publishEvent(
                ImageUpdatedEvent(
                    imageId = saved.id!!,
                    filename = saved.filename,
                    uuid = saved.uuid,
                    userId = saved.userId,
                ),
            )

            return GeneratedImageDto(
                filename = saved.filename,
                imageType = ImageType.GENERATED,
                promptId = saved.promptId,
                userId = saved.userId,
                generatedAt = saved.generatedAt,
                ipAddress = saved.ipAddress,
            )
        } catch (e: Exception) {
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
            imageService.delete(generatedImage.filename)

            // Delete from database
            generatedImageRepository.delete(generatedImage)

            // Publish event
            applicationEventPublisher.publishEvent(
                ImageDeletedEvent(
                    imageId = generatedImage.id!!,
                    filename = generatedImage.filename,
                    uuid = generatedImage.uuid,
                    userId = generatedImage.userId,
                ),
            )
        } catch (e: Exception) {
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
                    promptId = generatedImage.promptId,
                    userId = generatedImage.userId,
                    generatedAt = generatedImage.generatedAt,
                    ipAddress = generatedImage.ipAddress,
                )
            }
}
