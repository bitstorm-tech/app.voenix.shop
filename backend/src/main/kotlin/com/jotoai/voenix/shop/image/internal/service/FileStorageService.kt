package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.application.api.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.exceptions.ImageStorageException
import com.jotoai.voenix.shop.image.internal.entity.GeneratedImage
import com.jotoai.voenix.shop.image.internal.entity.UploadedImage
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.UUID

/**
 * Consolidated implementation of ImageStorageService that handles all storage functionality.
 * This service combines the functionality of BaseStorageService with user-specific storage operations,
 * and StoragePathService logic into a single cohesive service.
 */
@Service
@Suppress("TooManyFunctions")
class FileStorageService(
    private val storagePathService: StoragePathService,
    private val imageConversionService: ImageConversionService,
    private val uploadedImageRepository: UploadedImageRepository,
    private val generatedImageRepository: GeneratedImageRepository,
    private val imageValidationService: ImageValidationService,
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val ORIGINAL_SUFFIX = "_original"
        private const val GENERATED_PREFIX = "_generated_"
    }

    fun storeFile(
        file: MultipartFile,
        imageType: ImageType,
        cropArea: CropArea?,
    ): String {
        logger.debug { "Starting file storage - Type: $imageType, Original filename: ${file.originalFilename}" }

        imageValidationService.validateImageFile(file, imageType)

        val originalFilename = file.originalFilename ?: "unknown"
        val fileExtension = imageType.getFileExtension(originalFilename)
        // Do not mark cropped files as "original"; keep simple unique name
        val storedFilename = "${UUID.randomUUID()}$fileExtension"

        val targetPath = storagePathService.getPhysicalPath(imageType)
        val filePath = targetPath.resolve(storedFilename)

        logger.info { "Storing file - Target path: ${filePath.toAbsolutePath()}" }

        var imageBytes = file.bytes

        // Apply cropping if provided
        if (cropArea != null) {
            logger.debug { "Applying cropping with area: $cropArea" }
            imageBytes = imageConversionService.cropImage(imageBytes, cropArea)
        }

        // Apply format conversion if needed
        if (imageType.requiresWebPConversion) {
            logger.debug { "Converting image to WebP format" }
            imageBytes = imageConversionService.convertToWebP(imageBytes)
        }

        writeFile(filePath, imageBytes)
        logger.info { "Successfully stored image: ${filePath.toAbsolutePath()}" }

        return storedFilename
    }

    fun storeFile(
        bytes: ByteArray,
        originalFilename: String,
        imageType: ImageType,
    ): String {
        val fileExtension = getFileExtension(originalFilename)
        val storedFilename = "${UUID.randomUUID()}$fileExtension"

        val targetPath = storagePathService.getPhysicalPath(imageType)
        val filePath = targetPath.resolve(storedFilename)

        logger.info { "Storing image bytes - Target path: ${filePath.toAbsolutePath()}" }

        writeFile(filePath, bytes)
        logger.info { "Successfully stored image bytes: ${filePath.toAbsolutePath()}" }

        return storedFilename
    }

    fun loadFileAsBytes(
        filename: String,
        imageType: ImageType,
    ): ByteArray {
        val filePath = storagePathService.getPhysicalFilePath(imageType, filename)

        if (!Files.exists(filePath)) {
            throw ResourceNotFoundException("Image with filename $filename not found")
        }

        return Files.readAllBytes(filePath)
    }

    fun deleteFile(
        filename: String,
        imageType: ImageType,
    ): Boolean {
        val filePath = storagePathService.getPhysicalFilePath(imageType, filename)
        return deleteFile(filePath)
    }

    fun getImageData(
        filename: String,
        userId: Long?,
    ): Pair<ByteArray, String> =
        when {
            userId != null -> validateAccessAndGetImageData(filename, userId)
            else -> {
                val imageType =
                    storagePathService.findImageTypeByFilename(filename)
                        ?: throw ResourceNotFoundException("Image with filename $filename not found")
                val bytes = loadFileAsBytes(filename, imageType)
                val filePath = storagePathService.getPhysicalFilePath(imageType, filename)
                val contentType = probeContentType(filePath)
                Pair(bytes, contentType)
            }
        }

    fun serveUserImage(
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
     * Stores an uploaded image file using the user-specific storage pattern.
     */
    @Transactional
    fun storeUploadedImage(
        imageFile: MultipartFile,
        userId: Long,
        cropArea: CropArea?,
    ): UploadedImage {
        imageValidationService.validateImageFile(imageFile, ImageType.PRIVATE)

        val uuid = UUID.randomUUID()
        val originalFilename = imageFile.originalFilename ?: "unknown"
        val userStorageDir = getUserStorageDirectory(userId)

        // Determine file extension - convert to PNG if not already PNG
        val contentType = imageFile.contentType?.lowercase() ?: ""
        val isPng = contentType == "image/png"
        val fileExtension = ".png" // Always store as PNG for consistency

        val storedFilename = "$uuid$ORIGINAL_SUFFIX$fileExtension"
        val filePath = userStorageDir.resolve(storedFilename)

        logger.info { "Storing uploaded image for user $userId: $storedFilename" }

        // Create user directory if it doesn't exist
        ensureDirectoryExists(userStorageDir)

        var imageBytes = imageFile.bytes

        // Apply cropping if provided
        if (cropArea != null) {
            logger.debug { "Applying cropping to user upload with area: $cropArea" }
            imageBytes = imageConversionService.cropImage(imageBytes, cropArea)
        }

        // Convert to PNG if necessary
        if (!isPng) {
            logger.debug { "Converting image from $contentType to PNG" }
            imageBytes = imageConversionService.convertToPng(imageBytes)
        }

        writeFile(filePath, imageBytes)
        logger.info { "Successfully stored uploaded image: ${filePath.toAbsolutePath()}" }

        // Save to database
        val uploadedImage =
            UploadedImage(
                uuid = uuid,
                originalFilename = originalFilename,
                storedFilename = storedFilename,
                contentType = "image/png", // Always PNG after conversion
                fileSize = imageBytes.size.toLong(),
                userId = userId,
                uploadedAt = LocalDateTime.now(),
            )

        return uploadedImageRepository.save(uploadedImage)
    }

    /**
     * Stores a generated image using the user-specific storage pattern.
     */
    @Transactional
    fun storeGeneratedImage(
        imageBytes: ByteArray,
        uploadedImage: UploadedImage,
        promptId: Long,
        generationNumber: Int,
        ipAddress: String?,
    ): GeneratedImage {
        val userStorageDir = getUserStorageDirectory(uploadedImage.userId)
        val storedFilename = "${uploadedImage.uuid}$GENERATED_PREFIX$generationNumber.png"
        val filePath = userStorageDir.resolve(storedFilename)

        logger.info { "Storing generated image for user ${uploadedImage.userId}: $storedFilename" }

        writeFile(filePath, imageBytes)
        logger.info { "Successfully stored generated image: ${filePath.toAbsolutePath()}" }

        // Save to database
        val generatedImage =
            GeneratedImage(
                filename = storedFilename,
                promptId = promptId,
                userId = uploadedImage.userId,
                uploadedImage = uploadedImage,
                generatedAt = LocalDateTime.now(),
                ipAddress = ipAddress,
            )

        val savedImage = generatedImageRepository.save(generatedImage)
        // Flush to ensure ID is generated immediately
        generatedImageRepository.flush()
        logger.info { "Generated image saved with ID: ${savedImage.id}" }
        return savedImage
    }

    /**
     * Retrieves image data for a user's image file.
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

        val bytes = readFile(filePath)
        val contentType = probeContentType(filePath, "image/png")
        return Pair(bytes, contentType)
    }

    fun deleteUserImage(
        filename: String,
        userId: Long,
    ): Boolean {
        val userStorageDir = getUserStorageDirectory(userId)
        val filePath = userStorageDir.resolve(filename)
        return deleteFile(filePath)
    }

    private fun ensureDirectoryExists(directoryPath: Path) {
        try {
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath)
                logger.debug { "Created directory: ${directoryPath.toAbsolutePath()}" }
            }
        } catch (e: IOException) {
            logger.error(e) { "Failed to create directory ${directoryPath.toAbsolutePath()}: ${e.message}" }
            throw ImageStorageException("Failed to create directory: ${e.message}", e)
        }
    }

    private fun writeFile(
        filePath: Path,
        bytes: ByteArray,
    ) {
        try {
            Files.write(filePath, bytes)
            logger.debug { "Successfully wrote file: ${filePath.toAbsolutePath()}" }
        } catch (e: IOException) {
            logger.error(e) { "Failed to write file at ${filePath.toAbsolutePath()}: ${e.message}" }
            throw ImageStorageException("Failed to write file: ${e.message}", e)
        }
    }

    private fun readFile(filePath: Path): ByteArray {
        try {
            return Files.readAllBytes(filePath)
        } catch (e: IOException) {
            logger.error(e) { "Failed to read file ${filePath.toAbsolutePath()}: ${e.message}" }
            throw ImageStorageException("Failed to read file: ${e.message}", e)
        }
    }

    private fun deleteFile(filePath: Path): Boolean {
        try {
            return Files.deleteIfExists(filePath)
        } catch (e: IOException) {
            logger.error(e) { "Failed to delete file ${filePath.toAbsolutePath()}: ${e.message}" }
            throw ImageStorageException("Failed to delete file: ${e.message}", e)
        }
    }

    private fun probeContentType(
        filePath: Path,
        defaultContentType: String = "application/octet-stream",
    ): String =
        try {
            Files.probeContentType(filePath) ?: defaultContentType
        } catch (_: IOException) {
            logger.warn {
                "Failed to probe content type for ${filePath.toAbsolutePath()}, " +
                    "using default: $defaultContentType"
            }
            defaultContentType
        }

    private fun getFileExtension(filename: String): String {
        val lastDotIndex = filename.lastIndexOf('.')
        return if (lastDotIndex > 0) filename.substring(lastDotIndex) else ""
    }

    private fun getUserStorageDirectory(userId: Long): Path {
        val storageRoot = storagePathService.getStorageRoot()
        return storageRoot.resolve("private").resolve("images").resolve(userId.toString())
    }

    private fun validateAccessAndGetImageData(
        filename: String,
        userId: Long,
    ): Pair<ByteArray, String> {
        logger.debug { "Validating access to image $filename for user $userId" }
        validateImageAccess(filename, userId)
        return getUserImageData(filename, userId)
    }

    private fun validateImageAccess(
        filename: String,
        userId: Long,
    ) {
        val imageInfo =
            parseImageFilename(filename)
                ?: throw ResourceNotFoundException("Invalid image filename format")

        when (imageInfo.type) {
            ImageFileType.ORIGINAL -> {
                uploadedImageRepository.findByUserIdAndUuid(userId, imageInfo.uuid)
                    ?: throw ResourceNotFoundException("Uploaded image not found or access denied")
                logger.debug { "Access granted to original image $filename for user $userId" }
            }
            ImageFileType.GENERATED -> {
                val generatedImage =
                    generatedImageRepository.findByFilename(filename)
                        ?: throw ResourceNotFoundException("Generated image not found")
                require(generatedImage.userId == userId) {
                    throw ResourceNotFoundException("Generated image not found or access denied")
                }
                logger.debug { "Access granted to generated image $filename for user $userId" }
            }
        }
    }

    private fun parseImageFilename(filename: String): ImageInfo? =
        when {
            filename.contains(ORIGINAL_SUFFIX) -> {
                val uuidString = filename.substringBefore(ORIGINAL_SUFFIX)
                try {
                    val uuid = UUID.fromString(uuidString)
                    ImageInfo(uuid, ImageFileType.ORIGINAL)
                } catch (e: IllegalArgumentException) {
                    logger.error(e) { "Invalid UUID format in filename: $filename" }
                    null
                }
            }
            filename.contains(GENERATED_PREFIX) -> {
                // For generated images, we don't extract UUID from filename
                // We use the filename directly for lookup
                ImageInfo(UUID.randomUUID(), ImageFileType.GENERATED) // UUID not used for generated
            }
            else -> null
        }

    private data class ImageInfo(
        val uuid: UUID,
        val type: ImageFileType,
    )

    private enum class ImageFileType {
        ORIGINAL,
        GENERATED,
    }
}
