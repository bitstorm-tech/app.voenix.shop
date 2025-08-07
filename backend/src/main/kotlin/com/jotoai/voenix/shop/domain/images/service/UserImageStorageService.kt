package com.jotoai.voenix.shop.domain.images.service

import com.jotoai.voenix.shop.common.exception.BadRequestException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.images.entity.GeneratedImage
import com.jotoai.voenix.shop.domain.images.entity.UploadedImage
import com.jotoai.voenix.shop.domain.images.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.domain.images.repository.UploadedImageRepository
import com.jotoai.voenix.shop.user.internal.entity.User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.UUID

/**
 * Service responsible for managing user-specific image storage following the pattern:
 * {storage.root}/private/images/{userId}/{uuid}_original.{ext}
 * {storage.root}/private/images/{userId}/{uuid}_generated_{1|2|3|4...}.{ext}
 */
@Service
@Transactional(readOnly = true)
class UserImageStorageService(
    private val uploadedImageRepository: UploadedImageRepository,
    private val generatedImageRepository: GeneratedImageRepository,
    private val storagePathService: StoragePathService,
    private val imageConversionService: ImageConversionService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserImageStorageService::class.java)
        private const val MAX_FILE_SIZE = 10 * 1024 * 1024 // 10MB
        private val ALLOWED_CONTENT_TYPES = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")
        private const val ORIGINAL_SUFFIX = "_original"
        private const val GENERATED_PREFIX = "_generated_"
    }

    /**
     * Stores an uploaded image file using the new storage pattern.
     * @param imageFile The uploaded file
     * @param user The user who uploaded the image
     * @return The created UploadedImage entity
     */
    @Transactional
    fun storeUploadedImage(
        imageFile: MultipartFile,
        user: User,
    ): UploadedImage {
        validateImageFile(imageFile)

        val uuid = UUID.randomUUID()
        val originalFilename = imageFile.originalFilename ?: "unknown"
        val userStorageDir = getUserStorageDirectory(user.id!!)

        // Determine file extension - convert to PNG if not already PNG
        val contentType = imageFile.contentType?.lowercase() ?: ""
        val isPng = contentType == "image/png"
        val fileExtension = if (isPng) ".png" else ".png" // Always store as PNG for consistency

        val storedFilename = "$uuid$ORIGINAL_SUFFIX$fileExtension"
        val filePath = userStorageDir.resolve(storedFilename)

        logger.info("Storing uploaded image for user ${user.id}: $storedFilename")

        try {
            // Create user directory if it doesn't exist
            Files.createDirectories(userStorageDir)

            var imageBytes = imageFile.bytes

            // Convert to PNG if necessary
            if (!isPng) {
                logger.debug("Converting image from $contentType to PNG")
                imageBytes = imageConversionService.convertToPng(imageBytes)
            }

            Files.write(filePath, imageBytes)
            logger.info("Successfully stored uploaded image: ${filePath.toAbsolutePath()}")

            // Save to database
            val uploadedImage =
                UploadedImage(
                    uuid = uuid,
                    originalFilename = originalFilename,
                    storedFilename = storedFilename,
                    contentType = "image/png", // Always PNG after conversion
                    fileSize = imageBytes.size.toLong(),
                    user = user,
                    uploadedAt = LocalDateTime.now(),
                )

            return uploadedImageRepository.save(uploadedImage)
        } catch (e: IOException) {
            logger.error("Failed to store uploaded image at ${filePath.toAbsolutePath()}: ${e.message}", e)
            throw RuntimeException("Failed to store uploaded image: ${e.message}", e)
        }
    }

    /**
     * Stores a generated image using the new storage pattern.
     * @param imageBytes The generated image bytes
     * @param uploadedImage The original uploaded image this was generated from
     * @param promptId The ID of the prompt used for generation
     * @param generationNumber The generation number (1, 2, 3, etc.)
     * @return The created GeneratedImage entity
     */
    @Transactional
    fun storeGeneratedImage(
        imageBytes: ByteArray,
        uploadedImage: UploadedImage,
        promptId: Long,
        generationNumber: Int,
    ): GeneratedImage {
        val userStorageDir = getUserStorageDirectory(uploadedImage.user.id!!)
        val storedFilename = "${uploadedImage.uuid}$GENERATED_PREFIX$generationNumber.png"
        val filePath = userStorageDir.resolve(storedFilename)

        logger.info("Storing generated image for user ${uploadedImage.user.id}: $storedFilename")

        try {
            Files.write(filePath, imageBytes)
            logger.info("Successfully stored generated image: ${filePath.toAbsolutePath()}")

            // Save to database
            val generatedImage =
                GeneratedImage(
                    filename = storedFilename,
                    promptId = promptId,
                    user = uploadedImage.user,
                    uploadedImage = uploadedImage,
                    generatedAt = LocalDateTime.now(),
                )

            return generatedImageRepository.save(generatedImage)
        } catch (e: IOException) {
            logger.error("Failed to store generated image at ${filePath.toAbsolutePath()}: ${e.message}", e)
            throw RuntimeException("Failed to store generated image: ${e.message}", e)
        }
    }

    /**
     * Retrieves image data for a user's image file.
     * @param filename The image filename
     * @param userId The user ID (for security validation)
     * @return Pair of image bytes and content type
     */
    fun getUserImageData(
        filename: String,
        userId: Long,
    ): Pair<ByteArray, String> {
        val userStorageDir = getUserStorageDirectory(userId)
        val filePath = userStorageDir.resolve(filename)

        if (!Files.exists(filePath)) {
            throw ResourceNotFoundException("Image $filename not found for user $userId")
        }

        // Validate that this file belongs to the user by checking the directory structure
        val actualUserDir = filePath.parent
        if (actualUserDir != userStorageDir) {
            throw ResourceNotFoundException("Image $filename not found for user $userId")
        }

        try {
            val bytes = Files.readAllBytes(filePath)
            val contentType = Files.probeContentType(filePath) ?: "image/png"
            return Pair(bytes, contentType)
        } catch (e: IOException) {
            logger.error("Failed to read image file $filename for user $userId", e)
            throw RuntimeException("Failed to read image file: ${e.message}", e)
        }
    }

    /**
     * Gets the uploaded image entity by UUID and validates user ownership.
     */
    fun getUploadedImageByUuid(
        uuid: UUID,
        userId: Long,
    ): UploadedImage =
        uploadedImageRepository.findByUserIdAndUuid(userId, uuid)
            ?: throw ResourceNotFoundException("Uploaded image with UUID $uuid not found for user $userId")

    /**
     * Gets all uploaded images for a user.
     */
    fun getUserUploadedImages(userId: Long): List<UploadedImage> = uploadedImageRepository.findAllByUserId(userId)

    /**
     * Gets the user-specific storage directory path.
     */
    private fun getUserStorageDirectory(userId: Long): Path {
        val storageRoot = storagePathService.getStorageRoot()
        return storageRoot.resolve("private").resolve("images").resolve(userId.toString())
    }

    private fun validateImageFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw BadRequestException("Image file is required")
        }

        if (file.size > MAX_FILE_SIZE) {
            throw BadRequestException("Image file size must be less than 10MB")
        }

        val contentType = file.contentType?.lowercase() ?: ""
        if (contentType !in ALLOWED_CONTENT_TYPES) {
            throw BadRequestException("Invalid image format. Allowed formats: JPEG, PNG, WebP")
        }
    }
}
