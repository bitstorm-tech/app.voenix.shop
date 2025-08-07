package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.dto.CreateImageRequest
import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.image.api.dto.ImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.SimpleImageDto
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/**
 * Service responsible for pure file storage operations for images.
 * Handles image storage, retrieval, deletion, and conversion/cropping operations.
 * Does not handle business logic or entity persistence - only file operations.
 */
@Service
class ImageStorageService(
    private val imageConversionService: ImageConversionService,
    private val storagePathService: com.jotoai.voenix.shop.image.api.StoragePathService,
) : BaseStorageService() {
    init {
        logger.info("Initializing ImageStorageService with StoragePathService")
        logger.info("Storage root: ${storagePathService.getStorageRoot()}")
    }

    /**
     * Stores an image file with optional cropping and format conversion.
     * This method only handles file operations and does not create any database entities.
     */
    fun storeImage(
        file: MultipartFile,
        request: CreateImageRequest,
    ): ImageDto {
        logger.debug("Starting file storage - Type: {}, Original filename: {}", request.imageType, file.originalFilename)

        // Use ImageType configuration for validation
        validateFile(file, request.imageType.maxFileSize, request.imageType.allowedContentTypes.toSet())

        val originalFilename = file.originalFilename ?: "unknown"

        // Use ImageType configuration to determine file extension
        val fileExtension = request.imageType.getFileExtension(originalFilename)
        val storedFilename = "${UUID.randomUUID()}$fileExtension"

        val targetPath = storagePathService.getPhysicalPath(request.imageType)
        val filePath = targetPath.resolve(storedFilename)

        logger.info("Storing file - Target path: ${filePath.toAbsolutePath()}")

        try {
            var imageBytes = file.bytes

            // Apply cropping if requested
            if (request.cropArea != null) {
                imageBytes = applyCropping(imageBytes, request.cropArea)
            }

            // Apply format conversion if needed
            if (request.imageType.requiresWebPConversion) {
                logger.debug("Converting image to WebP format")
                val webpBytes = imageConversionService.convertToWebP(imageBytes)
                writeFile(filePath, webpBytes)
                logger.info("Successfully stored WebP image: ${filePath.toAbsolutePath()}")
            } else {
                // Store the image (cropped or original)
                writeFile(filePath, imageBytes)
                logger.info("Successfully stored image: ${filePath.toAbsolutePath()}")
            }
        } catch (e: IllegalArgumentException) {
            logger.error("Invalid image processing parameters: ${e.message}", e)
            throw IllegalArgumentException("Invalid image processing parameters: ${e.message}", e)
        }

        return SimpleImageDto(
            filename = storedFilename,
            imageType = request.imageType,
        )
    }

    /**
     * Stores raw image bytes directly.
     * Used for storing generated images where we already have the bytes.
     */
    fun storeImageBytes(
        imageBytes: ByteArray,
        filename: String,
        imageType: ImageType,
    ) {
        val targetPath = storagePathService.getPhysicalPath(imageType)
        val filePath = targetPath.resolve(filename)

        logger.info("Storing image bytes - Target path: ${filePath.toAbsolutePath()}")

        writeFile(filePath, imageBytes)
        logger.info("Successfully stored image bytes: ${filePath.toAbsolutePath()}")
    }

    /**
     * Retrieves image data from storage.
     */
    fun getImageData(
        filename: String,
        imageType: ImageType,
    ): Pair<ByteArray, String> {
        val filePath = storagePathService.getPhysicalFilePath(imageType, filename)

        if (!fileExists(filePath)) {
            throw ResourceNotFoundException("Image with filename $filename not found")
        }

        val bytes = readFile(filePath)
        val contentType = probeContentType(filePath, "application/octet-stream")
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
        val filePath = storagePathService.getPhysicalFilePath(imageType, filename)

        if (!deleteFile(filePath)) {
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

    /**
     * Checks if an image file exists in storage.
     */
    fun imageExists(
        filename: String,
        imageType: ImageType,
    ): Boolean {
        val filePath = storagePathService.getPhysicalFilePath(imageType, filename)
        return fileExists(filePath)
    }

    /**
     * Applies cropping to image bytes using the conversion service.
     */
    private fun applyCropping(
        imageBytes: ByteArray,
        cropArea: CropArea,
    ): ByteArray {
        // Get original image dimensions for logging
        val originalImage = imageConversionService.getImageDimensions(imageBytes)
        logger.info(
            "Applying crop - Original image: ${originalImage.width}x${originalImage.height}, " +
                "Crop area: x=${cropArea.x}, y=${cropArea.y}, " +
                "width=${cropArea.width}, height=${cropArea.height}",
        )

        val croppedBytes = imageConversionService.cropImage(imageBytes, cropArea)
        val croppedImage = imageConversionService.getImageDimensions(croppedBytes)
        logger.info("Crop result - New dimensions: ${croppedImage.width}x${croppedImage.height}")

        return croppedBytes
    }
}
