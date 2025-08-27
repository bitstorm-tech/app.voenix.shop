package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.ImageStorageService
import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.exceptions.ImageStorageException
import com.jotoai.voenix.shop.image.internal.entity.GeneratedImage
import com.jotoai.voenix.shop.image.internal.entity.UploadedImage
import com.jotoai.voenix.shop.image.internal.repository.GeneratedImageRepository
import com.jotoai.voenix.shop.image.internal.repository.UploadedImageRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.*
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

/**
 * Consolidated implementation of ImageStorageService that handles all storage functionality.
 * This service combines the functionality of BaseStorageService, UserImageStorageService,
 * and StoragePathService logic into a single cohesive service.
 */
@Service
class ImageStorageServiceImpl(
    private val storagePathService: StoragePathService,
    private val imageConversionService: ImageConversionService,
    private val uploadedImageRepository: UploadedImageRepository,
    private val generatedImageRepository: GeneratedImageRepository,
    private val imageValidationService: ImageValidationService,
) : ImageStorageService, UserImageStorageService {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val ORIGINAL_SUFFIX = "_original"
        private const val GENERATED_PREFIX = "_generated_"
    }

    // Public API Interface Methods

    override fun storeFile(
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

    override fun storeFile(
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

    override fun loadFileAsResource(
        filename: String,
        imageType: ImageType,
    ): Resource {
        val filePath = storagePathService.getPhysicalFilePath(imageType, filename)

        if (!Files.exists(filePath)) {
            throw ResourceNotFoundException("Image with filename $filename not found")
        }

        return FileSystemResource(filePath)
    }

    override fun loadFileAsBytes(
        filename: String,
        imageType: ImageType,
    ): ByteArray {
        val filePath = storagePathService.getPhysicalFilePath(imageType, filename)

        if (!Files.exists(filePath)) {
            throw ResourceNotFoundException("Image with filename $filename not found")
        }

        return Files.readAllBytes(filePath)
    }

    // URL generation is handled by StoragePathService directly

    override fun deleteFile(
        filename: String,
        imageType: ImageType,
    ): Boolean {
        val filePath = storagePathService.getPhysicalFilePath(imageType, filename)
        return deleteFile(filePath)
    }

    override fun fileExists(
        filename: String,
        imageType: ImageType,
    ): Boolean {
        val filePath = storagePathService.getPhysicalFilePath(imageType, filename)
        return Files.exists(filePath)
    }

    // Internal helper methods for ImageManagementService

    /**
     * Retrieves image data from storage.
     */
    fun getImageData(
        filename: String,
        imageType: ImageType,
    ): Pair<ByteArray, String> {
        val resource = loadFileAsResource(filename, imageType)
        val bytes = resource.inputStream.use { it.readAllBytes() }
        val contentType = probeContentType(
            storagePathService.getPhysicalFilePath(imageType, filename),
            "application/octet-stream"
        )
        return Pair(bytes, contentType)
    }

    /**
     * Retrieves image data by filename only (searches across image types).
     */
    fun getImageData(filename: String): Pair<ByteArray, String> {
        val imageType =
            storagePathService.findImageTypeByFilename(filename)
                ?: throw ResourceNotFoundException("Image with filename $filename not found")
        return getImageData(filename, imageType)
    }

    /**
     * Deletes an image from storage.
     */
    fun deleteImage(
        filename: String,
        imageType: ImageType,
    ) {
        if (!deleteFile(filename, imageType)) {
            throw ResourceNotFoundException("Image with filename $filename not found")
        }
    }

    /**
     * Deletes an image by filename only (searches across image types).
     */
    fun deleteImage(filename: String) {
        val imageType =
            storagePathService.findImageTypeByFilename(filename)
                ?: throw ResourceNotFoundException("Image with filename $filename not found")
        deleteImage(filename, imageType)
    }

    // Mug Variant Storage Operations

    /**
     * Stores a mug variant example image with optional cropping.
     * Images are stored in the public mug variant example images directory.
     *
     * @param file The multipart file containing the image
     * @param cropArea Optional crop area to apply to the image
     * @return The filename of the stored image
     */
    fun storeMugVariantImage(
        file: MultipartFile,
        cropArea: CropArea? = null,
    ): String {
        logger.debug { "Storing mug variant image - Original filename: ${file.originalFilename}" }

        // Use the public mug variant example image type
        val imageType = ImageType.MUG_VARIANT_EXAMPLE

        // Validate the file using centralized rules
        imageValidationService.validateImageFile(file, imageType)

        // Generate a unique filename with PNG extension (we'll convert to PNG)
        val storedFilename = "${UUID.randomUUID()}.png"

        val targetPath = storagePathService.getPhysicalPath(imageType)
        val filePath = targetPath.resolve(storedFilename)

        logger.info { "Storing mug variant image - Target path: ${filePath.toAbsolutePath()}" }

        try {
            var imageBytes = file.bytes

            // Apply cropping if requested
            if (cropArea != null) {
                logger.debug {
                    "Applying crop - x: ${cropArea.x}, y: ${cropArea.y}, width: ${cropArea.width}, " +
                        "height: ${cropArea.height}"
                }
                imageBytes = applyCropping(imageBytes, cropArea)
            }

            // Convert to PNG for consistency
            imageBytes = imageConversionService.convertToPng(imageBytes)

            // Ensure the directory exists
            Files.createDirectories(targetPath)

            // Write the file
            writeFile(filePath, imageBytes)
            logger.info { "Successfully stored mug variant image: $storedFilename" }

            return storedFilename
        } catch (e: IOException) {
            logger.error(e) { "Failed to store mug variant image: ${e.message}" }
            // Try to clean up the file if it was partially written
            try {
                Files.deleteIfExists(filePath)
            } catch (cleanupEx: IOException) {
                logger.warn { "Failed to clean up partial file: ${cleanupEx.message}" }
            }
            throw ImageStorageException("Failed to store mug variant image: ${e.message}", e)
        }
    }

    /**
     * Deletes a mug variant image from storage.
     *
     * @param filename The filename of the image to delete
     * @return true if the file was deleted, false if it didn't exist
     */
    fun deleteMugVariantImage(filename: String): Boolean {
        if (filename.isBlank()) {
            return false
        }

        val imageType = ImageType.MUG_VARIANT_EXAMPLE
        val filePath = storagePathService.getPhysicalFilePath(imageType, filename)

        return try {
            val deleted = Files.deleteIfExists(filePath)
            if (deleted) {
                logger.info { "Deleted mug variant image: $filename" }
            } else {
                logger.debug { "Mug variant image not found for deletion: $filename" }
            }
            deleted
        } catch (e: IOException) {
            logger.error { "Error deleting mug variant image $filename: ${e.message}" }
            false
        }
    }

    // User-specific Storage Operations (formerly in UserImageStorageService)

    /**
     * Stores an uploaded image file using the user-specific storage pattern.
     */
    @Transactional
    override fun storeUploadedImage(
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
    override fun storeGeneratedImage(
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
    override fun getUserImageData(
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

    override fun deleteUserImage(
        filename: String,
        userId: Long,
    ): Boolean {
        val userStorageDir = getUserStorageDirectory(userId)
        val filePath = userStorageDir.resolve(filename)
        return deleteFile(filePath)
    }

    // Validation logic centralized in ImageValidationService

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

    private fun applyCropping(
        imageBytes: ByteArray,
        cropArea: CropArea,
    ): ByteArray {
        // Get original image dimensions for logging
        val originalImage = imageConversionService.getImageDimensions(imageBytes)

        // Check if transparent padding will be applied
        val hasNegativeCoords = cropArea.x < 0 || cropArea.y < 0
        val exceedsImageBounds =
            cropArea.x + cropArea.width > originalImage.width ||
                cropArea.y + cropArea.height > originalImage.height
        val requiresPadding = hasNegativeCoords || exceedsImageBounds

        logger.info {
            "Applying crop - Original image: ${originalImage.width}x${originalImage.height}, " +
                "Crop area: x=${cropArea.x}, y=${cropArea.y}, " +
                "width=${cropArea.width}, height=${cropArea.height}" +
                if (requiresPadding) " (with transparent padding)" else ""
        }

        val croppedBytes = imageConversionService.cropImage(imageBytes, cropArea)
        val croppedImage = imageConversionService.getImageDimensions(croppedBytes)
        logger.info { "Crop result - New dimensions: ${croppedImage.width}x${croppedImage.height}" }

        return croppedBytes
    }

    private fun getUserStorageDirectory(userId: Long): Path {
        val storageRoot = storagePathService.getStorageRoot()
        return storageRoot.resolve("private").resolve("images").resolve(userId.toString())
    }
}
