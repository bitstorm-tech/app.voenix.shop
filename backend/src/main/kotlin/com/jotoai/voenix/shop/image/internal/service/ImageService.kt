package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.dto.CreateImageRequest
import com.jotoai.voenix.shop.image.api.dto.ImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.SimpleImageDto
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class ImageService(
    private val imageConversionService: ImageConversionService,
    private val storagePathService: com.jotoai.voenix.shop.image.api.StoragePathService,
) : BaseStorageService() {
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

            // Use ImageType configuration to determine if WebP conversion is needed
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
            logger.error("Invalid crop parameters: ${e.message}", e)
            throw IllegalArgumentException("Invalid crop parameters: ${e.message}", e)
        }

        return SimpleImageDto(
            filename = storedFilename,
            imageType = request.imageType,
        )
    }

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

    fun delete(
        filename: String,
        imageType: ImageType,
    ) {
        val filePath = storagePathService.getPhysicalFilePath(imageType, filename)

        if (!deleteFile(filePath)) {
            throw ResourceNotFoundException("Image with filename $filename not found")
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
}
