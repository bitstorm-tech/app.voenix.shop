package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.application.api.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.UploadedImageDto
import com.jotoai.voenix.shop.image.api.exceptions.ImageException
import com.jotoai.voenix.shop.image.internal.entity.GeneratedImage
import com.jotoai.voenix.shop.image.internal.entity.UploadedImage
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.UUID

private fun UploadedImage.toDto() =
    UploadedImageDto(
        filename = storedFilename,
        imageType = ImageType.PRIVATE,
        id = id ?: 0L,
        uuid = uuid,
        originalFilename = originalFilename,
        contentType = contentType,
        fileSize = fileSize,
        uploadedAt = uploadedAt,
    )

private fun GeneratedImage.toDto() =
    GeneratedImageDto(
        filename = filename,
        imageType = ImageType.GENERATED,
        id = id ?: 0L,
        promptId = promptId,
        userId = userId,
        generatedAt = generatedAt,
        ipAddress = ipAddress,
    )

@Service
@Transactional
class ImageOperationsService(
    private val fileStorageService: FileStorageService,
    private val uploadedImageRepository: UploadedImageRepository,
    private val generatedImageRepository: GeneratedImageRepository,
    private val imageValidationService: ImageValidationService,
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private inline fun <T> wrapStorageException(
        operation: String,
        block: () -> T,
    ): T =
        try {
            block()
        } catch (e: Exception) {
            when (e) {
                is ImageException, is ResourceNotFoundException -> throw e
                else -> {
                    logger.error(e) { "$operation failed" }
                    throw ImageException.Storage("$operation: ${e.message}", e)
                }
            }
        }

    fun createUploadedImage(
        file: MultipartFile,
        userId: Long,
        imageType: ImageType,
        cropArea: CropArea?,
    ): UploadedImageDto =
        wrapStorageException("Create uploaded image") {
            logger.debug { "Creating uploaded image for user $userId with type $imageType" }

            if (imageType == ImageType.PRIVATE) {
                fileStorageService.storeUploadedImage(file, userId, cropArea).toDto()
            } else {
                val storedFilename = fileStorageService.storeFile(file, imageType, cropArea)
                createUploadedImageDto(storedFilename, file, imageType)
            }
        }

    @Transactional(readOnly = true)
    fun getUploadedImageByUuid(
        uuid: UUID,
        userId: Long,
    ): UploadedImageDto {
        val uploadedImage =
            uploadedImageRepository.findByUserIdAndUuid(userId, uuid)
                ?: throw ImageException.NotFound("Uploaded image with UUID $uuid not found for user $userId")

        return uploadedImage.toDto()
    }

    fun storeGeneratedImage(
        imageBytes: ByteArray,
        uploadedImageId: Long,
        promptId: Long,
        generationNumber: Int,
        ipAddress: String?,
    ): GeneratedImageDto =
        wrapStorageException("Store generated image") {
            logger.info { "Storing generated image for uploaded image ID: $uploadedImageId" }

            val uploadedImage =
                uploadedImageRepository.findById(uploadedImageId).orElseThrow {
                    ResourceNotFoundException("Uploaded image with ID $uploadedImageId not found")
                }

            val generatedImage =
                fileStorageService.storeGeneratedImage(
                    imageBytes = imageBytes,
                    uploadedImage = uploadedImage,
                    promptId = promptId,
                    generationNumber = generationNumber,
                    ipAddress = ipAddress,
                )

            logger.info { "Successfully stored generated image: ${generatedImage.filename}" }

            generatedImage.toDto()
        }

    fun storePublicGeneratedImage(
        imageBytes: ByteArray,
        promptId: Long,
        ipAddress: String,
        generationNumber: Int,
    ): GeneratedImageDto =
        wrapStorageException("Store public generated image") {
            logger.info { "Storing public generated image for prompt ID: $promptId" }

            val filename = "${UUID.randomUUID()}_generated_$generationNumber.png"
            fileStorageService.storeFile(imageBytes, filename, ImageType.PUBLIC)

            val generatedImage =
                GeneratedImage(
                    filename = filename,
                    promptId = promptId,
                    userId = null,
                    ipAddress = ipAddress,
                    generatedAt = LocalDateTime.now(),
                )
            val savedImage = generatedImageRepository.save(generatedImage)

            logger.info { "Successfully stored public generated image: $filename" }

            savedImage.toDto()
        }

    @Transactional(readOnly = true)
    fun countGeneratedImagesForIpAfter(
        ipAddress: String,
        after: LocalDateTime,
    ): Long = generatedImageRepository.countByIpAddressAndGeneratedAtAfter(ipAddress, after)

    @Transactional(readOnly = true)
    fun countGeneratedImagesForUserAfter(
        userId: Long,
        after: LocalDateTime,
    ): Long = generatedImageRepository.countByUserIdAndGeneratedAtAfter(userId, after)

    @Transactional(readOnly = true)
    fun validateImageFile(file: MultipartFile) {
        imageValidationService.validateImageFile(file)
    }

    private fun createUploadedImageDto(
        storedFilename: String,
        file: MultipartFile,
        imageType: ImageType,
    ): UploadedImageDto =
        UploadedImageDto(
            filename = storedFilename,
            imageType = imageType,
            uuid = UUID.randomUUID(),
            originalFilename = file.originalFilename ?: "unknown",
            contentType = file.contentType ?: "application/octet-stream",
            fileSize = file.size,
            uploadedAt = LocalDateTime.now(),
        )
}
