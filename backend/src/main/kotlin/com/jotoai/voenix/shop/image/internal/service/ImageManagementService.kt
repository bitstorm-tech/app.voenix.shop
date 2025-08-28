package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.common.exception.BadRequestException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.ImageAccessService
import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.image.api.ImageQueryService
import com.jotoai.voenix.shop.image.api.ImageStorageService
import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationRequest
import com.jotoai.voenix.shop.image.api.dto.PublicImageGenerationResponse
import com.jotoai.voenix.shop.image.api.dto.SimpleImageDto
import com.jotoai.voenix.shop.image.api.dto.UpdateGeneratedImageRequest
import com.jotoai.voenix.shop.image.api.dto.UploadedImageDto
import com.jotoai.voenix.shop.image.api.exceptions.ImageNotFoundException
import com.jotoai.voenix.shop.image.api.exceptions.ImageProcessingException
import com.jotoai.voenix.shop.image.api.exceptions.ImageStorageException
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import com.jotoai.voenix.shop.openai.api.OpenAIImageGenerationService
import com.jotoai.voenix.shop.user.api.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException
import java.time.LocalDateTime
import java.util.*
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

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
@Suppress("LongParameterList")
class ImageManagementService(
    private val imageStorageService: ImageStorageService,
    private val uploadedImageRepository: UploadedImageRepository,
    private val generatedImageRepository: GeneratedImageRepository,
    private val imageValidationService: ImageValidationService,
    private val openAIImageGenerationService: OpenAIImageGenerationService,
    private val storagePathService: StoragePathService,
    private val userService: UserService,
    private val userImageStorageService: UserImageStorageService,
    private val imageConversionService: ImageConversionService,
) : ImageFacade, ImageAccessService, ImageQueryService {
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val ORIGINAL_SUFFIX = "_original"
        private const val GENERATED_PREFIX = "_generated_"

        private const val PUBLIC_RATE_LIMIT_HOURS = 1
        private const val PUBLIC_MAX_GENERATIONS_PER_HOUR = 10
        private const val USER_RATE_LIMIT_HOURS = 24
        private const val USER_MAX_GENERATIONS_PER_DAY = 50
    }

    // Implementation of ImageFacade interface

    @Transactional
    override fun createUploadedImage(
        file: MultipartFile,
        userId: Long,
        cropArea: CropArea?,
    ): UploadedImageDto {
        // Delegate to the overloaded method with default PRIVATE type for backward compatibility
        return createUploadedImage(file, userId, ImageType.PRIVATE, cropArea)
    }

    @Transactional
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
            val storedFilename =
                when (imageType) {
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
                        imageStorageService.storeFile(file, imageType, cropArea)
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
            // Delete file from storage (private vs public)
            if (generatedImage.userId != null) {
                userImageStorageService.deleteUserImage(generatedImage.filename, generatedImage.userId!!)
            } else {
                imageStorageService.deleteFile(generatedImage.filename, ImageType.PUBLIC)
            }

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
        ipAddress: String?,
    ): GeneratedImageDto {
        try {
            logger.info { "Storing generated image for uploaded image ID: $uploadedImageId" }

            // Get the uploaded image to get the user ID
            val uploadedImage =
                uploadedImageRepository.findById(uploadedImageId).orElseThrow {
                    ResourceNotFoundException("Uploaded image with ID $uploadedImageId not found")
                }

            val generatedImage =
                userImageStorageService.storeGeneratedImage(
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
    ): PublicImageGenerationResponse {
        logger.info { "Generating image: user=$userId, prompt=$promptId" }

        userService.getUserById(userId)

        val dayAgo = LocalDateTime.now().minusHours(USER_RATE_LIMIT_HOURS.toLong())
        val count = countGeneratedImagesForUserAfter(userId, dayAgo)
        checkRateLimit(
            count,
            USER_MAX_GENERATIONS_PER_DAY,
            "Rate limit exceeded. Max $USER_MAX_GENERATIONS_PER_DAY images per day.",
        )

        try {
            return processImageGeneration(
                uploadedImageUuid,
                promptId,
                userId,
                null,
                cropArea,
            )
        } catch (e: BadRequestException) {
            throw e
        } catch (e: ImageStorageException) {
            handleSystemError(e, "user image generation")
        }
    }

    @Transactional
    override fun generatePublicImage(
        request: PublicImageGenerationRequest,
        ipAddress: String,
        imageFile: MultipartFile,
    ): PublicImageGenerationResponse {
        logger.info { "Generating image: public, prompt=${request.promptId}" }

        validateImageFile(imageFile)

        val hourAgo = LocalDateTime.now().minusHours(PUBLIC_RATE_LIMIT_HOURS.toLong())
        val count = countGeneratedImagesForIpAfter(ipAddress, hourAgo)
        checkRateLimit(
            count,
            PUBLIC_MAX_GENERATIONS_PER_HOUR,
            "Rate limit exceeded. Max $PUBLIC_MAX_GENERATIONS_PER_HOUR images per hour.",
        )

        try {
            return processPublicImageGeneration(
                imageFile,
                request.promptId,
                ipAddress,
                request.cropArea,
            )
        } catch (e: BadRequestException) {
            throw e
        } catch (e: ImageStorageException) {
            handleSystemError(e, "public image generation")
        }
    }

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

        validateImageAccess(filename, userId)

        // If validation passes, get the image data through the storage service
        return userImageStorageService.getUserImageData(filename, userId)
    }

    private fun validateImageAccess(filename: String, userId: Long) {
        // Check if this is an original image or generated image based on filename pattern
        val isOriginalImage = filename.contains(ORIGINAL_SUFFIX)
        val isGeneratedImage = filename.contains(GENERATED_PREFIX)

        when {
            isOriginalImage -> validateOriginalImageAccess(filename, userId)
            isGeneratedImage -> validateGeneratedImageAccess(filename, userId)
            else -> throw ResourceNotFoundException("Invalid image filename format")
        }
    }

    private fun validateOriginalImageAccess(filename: String, userId: Long) {
        // Extract UUID from filename (format: {uuid}_original.{ext})
        val uuid = extractUuidFromOriginalFilename(filename)
        uploadedImageRepository.findByUserIdAndUuid(userId, uuid)
            ?: throw ResourceNotFoundException("Uploaded image not found or access denied")

        logger.debug { "Access granted to original image $filename for user $userId" }
    }

    private fun validateGeneratedImageAccess(filename: String, userId: Long) {
        // For generated images, check ownership through the generated_images table
        val generatedImage =
            generatedImageRepository.findByFilename(filename)
                ?: throw ResourceNotFoundException("Generated image not found")

        if (generatedImage.userId != userId) {
            throw ResourceNotFoundException("Generated image not found or access denied")
        }

        logger.debug { "Access granted to generated image $filename for user $userId" }
    }

    /**
     * Returns raw image data for internal use or public access.
     */
    override fun getImageData(
        filename: String,
        userId: Long?,
    ): Pair<ByteArray, String> =
        when {
            userId != null -> validateAccessAndGetImageData(filename, userId)
            else -> {
                val imageType =
                    storagePathService.findImageTypeByFilename(filename)
                        ?: throw ResourceNotFoundException("Image with filename $filename not found")
                val bytes = imageStorageService.loadFileAsBytes(filename, imageType)
                val filePath = storagePathService.getPhysicalFilePath(imageType, filename)
                val contentType = try {
                    java.nio.file.Files.probeContentType(filePath) ?: "application/octet-stream"
                } catch (_: java.io.IOException) {
                    "application/octet-stream"
                }
                Pair(bytes, contentType)
            }
        }

    override fun serveUserImage(
        filename: String,
        userId: Long,
    ): ResponseEntity<Resource> {
        val (imageData, contentType) = validateAccessAndGetImageData(filename, userId)
        return ResponseEntity
            .ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"$filename\"")
            .contentType(MediaType.parseMediaType(contentType))
            .body(ByteArrayResource(imageData))
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

    private fun checkRateLimit(
        count: Long,
        limit: Int,
        errorMessage: String,
    ) {
        if (count >= limit) {
            throw BadRequestException(errorMessage)
        }
    }

    private fun processImageGeneration(
        uploadedImageUuid: UUID,
        promptId: Long,
        userId: Long,
        ipAddress: String?,
        cropArea: CropArea?,
    ): PublicImageGenerationResponse {
        val uploadedImage = getUploadedImageByUuid(uploadedImageUuid, userId)

        val originalBytes = imageStorageService.loadFileAsBytes(uploadedImage.filename, ImageType.PRIVATE)
        val imageBytes =
            if (cropArea != null) {
                imageConversionService.cropImage(originalBytes, cropArea)
            } else {
                originalBytes
            }

        val generatedBytes = openAIImageGenerationService.generateImages(imageBytes, promptId)

        val uploadedImageEntity =
            uploadedImage.id?.let { id -> uploadedImageRepository.findById(id).orElse(null) }
                ?: uploadedImageRepository.findByUserIdAndUuid(userId, uploadedImageUuid)
                ?: throw ResourceNotFoundException("Uploaded image not found")

        val generatedImages =
            generatedBytes.mapIndexed { index, bytes ->
                storeGeneratedImage(
                    imageBytes = bytes,
                    uploadedImageId = uploadedImageEntity.id!!,
                    promptId = promptId,
                    generationNumber = index + 1,
                    ipAddress = ipAddress,
                )
            }

        val imageUrls = generatedImages.map { dto -> storagePathService.getImageUrl(ImageType.PRIVATE, dto.filename) }

        val imageIds = generatedImages.mapNotNull { it.id }
        logger.info { "Generated ${imageIds.size} images" }

        return PublicImageGenerationResponse(
            imageUrls = imageUrls,
            generatedImageIds = imageIds,
        )
    }

    private fun processPublicImageGeneration(
        imageFile: MultipartFile,
        promptId: Long,
        ipAddress: String,
        cropArea: CropArea?,
    ): PublicImageGenerationResponse {
        val originalBytes = imageFile.bytes
        val imageBytes =
            if (cropArea != null) {
                imageConversionService.cropImage(originalBytes, cropArea)
            } else {
                originalBytes
            }
        val generatedBytes = openAIImageGenerationService.generateImages(imageBytes, promptId)

        val generatedImages =
            generatedBytes.mapIndexed { index, bytes ->
                storePublicGeneratedImage(
                    imageBytes = bytes,
                    promptId = promptId,
                    ipAddress = ipAddress,
                    generationNumber = index + 1,
                )
            }

        val imageUrls =
            generatedImages.map { generatedImageDto ->
                storagePathService.getImageUrl(ImageType.PUBLIC, generatedImageDto.filename)
            }

        val imageIds = generatedImages.mapNotNull { it.id }
        logger.info { "Generated ${imageIds.size} images" }

        return PublicImageGenerationResponse(
            imageUrls = imageUrls,
            generatedImageIds = imageIds,
        )
    }

    private fun handleSystemError(
        e: Exception,
        operation: String,
    ): Nothing {
        logger.error(e) { "Error during $operation" }
        throw ImageProcessingException("Failed to generate image. Please try again later.", e)
    }

    // ImageQueryService implementation
    override fun findUploadedImageByUuid(uuid: UUID): ImageDto? {
        val uploaded = uploadedImageRepository.findByUuid(uuid)
        return uploaded?.let {
            SimpleImageDto(filename = it.storedFilename, imageType = ImageType.PRIVATE)
        }
    }

    override fun findUploadedImagesByUserId(userId: Long): List<ImageDto> =
        uploadedImageRepository.findAllByUserId(userId).map {
            SimpleImageDto(filename = it.storedFilename, imageType = ImageType.PRIVATE)
        }

    override fun existsByUuid(uuid: UUID): Boolean = uploadedImageRepository.findByUuid(uuid) != null

    override fun existsByUuidAndUserId(uuid: UUID, userId: Long): Boolean =
        uploadedImageRepository.findByUserIdAndUuid(userId, uuid) != null

    override fun existsGeneratedImageById(id: Long): Boolean = generatedImageRepository.existsById(id)

    override fun existsGeneratedImageByIdAndUserId(id: Long, userId: Long): Boolean =
        generatedImageRepository.existsByIdAndUserId(id, userId)

    override fun validateGeneratedImageOwnership(imageId: Long, userId: Long?): Boolean =
        if (userId != null) existsGeneratedImageByIdAndUserId(imageId, userId) else existsGeneratedImageById(imageId)

    override fun findGeneratedImageById(id: Long): GeneratedImageDto? {
        val generated = generatedImageRepository.findById(id).orElse(null)
        return generated?.let {
            GeneratedImageDto(
                filename = it.filename,
                imageType = ImageType.GENERATED,
                promptId = it.promptId,
                userId = it.userId,
                generatedAt = it.generatedAt,
                ipAddress = it.ipAddress,
            )
        }
    }

    override fun findGeneratedImagesByIds(ids: List<Long>): Map<Long, GeneratedImageDto> {
        if (ids.isEmpty()) return emptyMap()
        return generatedImageRepository.findAllById(ids).associateBy(
            { requireNotNull(it.id) { "GeneratedImage ID cannot be null" } },
            {
                GeneratedImageDto(
                    filename = it.filename,
                    imageType = ImageType.GENERATED,
                    promptId = it.promptId,
                    userId = it.userId,
                    generatedAt = it.generatedAt,
                    ipAddress = it.ipAddress,
                )
            },
        )
    }
}
