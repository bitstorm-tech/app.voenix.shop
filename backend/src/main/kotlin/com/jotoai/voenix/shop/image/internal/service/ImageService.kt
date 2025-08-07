package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.dto.CreateImageRequest
import com.jotoai.voenix.shop.image.api.dto.ImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.util.UUID

@Service
class ImageService(
    private val imageConversionService: ImageConversionService,
    private val storagePathService: StoragePathService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ImageService::class.java)
    }

    init {
        logger.info("Initializing ImageService with StoragePathService")
        logger.info("Storage root: ${storagePathService.getStorageRoot()}")
        // Directory creation is now handled by StoragePathService
    }

    fun store(
        file: MultipartFile,
        request: CreateImageRequest,
    ): ImageDto {
        logger.debug("Starting file upload - Type: {}, Original filename: {}", request.imageType, file.originalFilename)
        validateFile(file)

        val originalFilename = file.originalFilename ?: "unknown"

        // For prompt, slot and all variant examples, always use .webp extension
        val fileExtension =
            if (request.imageType == ImageType.PROMPT_EXAMPLE ||
                request.imageType == ImageType.PROMPT_SLOT_VARIANT_EXAMPLE ||
                request.imageType == ImageType.MUG_VARIANT_EXAMPLE ||
                request.imageType == ImageType.SHIRT_VARIANT_EXAMPLE
            ) {
                ".webp"
            } else {
                getFileExtension(originalFilename)
            }
        val storedFilename = "${UUID.randomUUID()}$fileExtension"

        val targetPath = storagePathService.getPhysicalPath(request.imageType)
        val filePath = targetPath.resolve(storedFilename)

        logger.info("Storing file - Target path: ${filePath.toAbsolutePath()}")

        try {
            var imageBytes = file.bytes

            // Apply cropping if requested
            if (request.cropArea != null) {
                // Get original image dimensions for logging
                val originalImage = imageConversionService.getImageDimensions(imageBytes)
                logger.info(
                    "Applying crop - Original image: ${originalImage.width}x${originalImage.height}, " +
                        "Crop area: x=${request.cropArea.x}, y=${request.cropArea.y}, " +
                        "width=${request.cropArea.width}, height=${request.cropArea.height}",
                )
                imageBytes = imageConversionService.cropImage(imageBytes, request.cropArea)
                val croppedImage = imageConversionService.getImageDimensions(imageBytes)
                logger.info("Crop result - New dimensions: ${croppedImage.width}x${croppedImage.height}")
            }

            if (request.imageType == ImageType.PROMPT_EXAMPLE ||
                request.imageType == ImageType.PROMPT_SLOT_VARIANT_EXAMPLE ||
                request.imageType == ImageType.MUG_VARIANT_EXAMPLE ||
                request.imageType == ImageType.SHIRT_VARIANT_EXAMPLE
            ) {
                // Convert to WebP for prompt, slot and all variant examples
                logger.debug("Converting image to WebP format")
                val webpBytes = imageConversionService.convertToWebP(imageBytes)
                Files.write(filePath, webpBytes)
                logger.info("Successfully stored WebP image: ${filePath.toAbsolutePath()}")
            } else {
                // Store the image (cropped or original)
                Files.write(filePath, imageBytes)
                logger.info("Successfully stored image: ${filePath.toAbsolutePath()}")
            }
        } catch (e: IOException) {
            logger.error("Failed to store file at ${filePath.toAbsolutePath()}: ${e.message}", e)
            throw RuntimeException("Failed to store file: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid crop parameters: ${e.message}", e)
            throw IllegalArgumentException("Invalid crop parameters: ${e.message}", e)
        }

        return ImageDto(
            filename = storedFilename,
            imageType = request.imageType,
        )
    }

    fun getImageData(
        filename: String,
        imageType: ImageType,
    ): Pair<ByteArray, String> {
        val filePath = storagePathService.getPhysicalFilePath(imageType, filename)

        if (!Files.exists(filePath)) {
            throw ResourceNotFoundException("Image with filename $filename not found")
        }

        return try {
            val bytes = Files.readAllBytes(filePath)
            val contentType = Files.probeContentType(filePath) ?: "application/octet-stream"
            Pair(bytes, contentType)
        } catch (e: IOException) {
            throw RuntimeException("Failed to read file: ${e.message}", e)
        }
    }

    fun delete(
        filename: String,
        imageType: ImageType,
    ) {
        val filePath = storagePathService.getPhysicalFilePath(imageType, filename)

        try {
            if (!Files.deleteIfExists(filePath)) {
                throw ResourceNotFoundException("Image with filename $filename not found")
            }
        } catch (e: IOException) {
            throw RuntimeException("Failed to delete file: ${e.message}", e)
        }
    }

    fun getImageData(filename: String): Pair<ByteArray, String> {
        val imageType =
            storagePathService.findImageTypeByFilename(filename)
                ?: throw ResourceNotFoundException("Image with filename $filename not found")
        return getImageData(filename, imageType)
    }

    fun delete(filename: String) {
        val imageType =
            storagePathService.findImageTypeByFilename(filename)
                ?: throw ResourceNotFoundException("Image with filename $filename not found")
        delete(filename, imageType)
    }

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw IllegalArgumentException("Cannot upload empty file")
        }

        val contentType = file.contentType
        if (contentType == null || !contentType.startsWith("image/")) {
            throw IllegalArgumentException("File must be an image")
        }

        val maxSize = 10 * 1024 * 1024 // 10MB
        if (file.size > maxSize) {
            throw IllegalArgumentException("File size exceeds maximum allowed size of 10MB")
        }
    }

    private fun getFileExtension(filename: String): String {
        val lastDotIndex = filename.lastIndexOf('.')
        return if (lastDotIndex > 0) filename.substring(lastDotIndex) else ""
    }
}
