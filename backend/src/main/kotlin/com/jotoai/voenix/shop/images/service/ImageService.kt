package com.jotoai.voenix.shop.images.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.images.dto.CreateImageRequest
import com.jotoai.voenix.shop.images.dto.ImageDto
import com.jotoai.voenix.shop.images.dto.ImageType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

@Service
class ImageService(
    @Value("\${images.storage.root:storage}") private val storageRoot: String,
    private val imageConversionService: ImageConversionService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ImageService::class.java)
    }

    private val rootPath: Path = Paths.get(storageRoot).toAbsolutePath()
    private val privateImagesPath: Path = rootPath.resolve("private/images")
    private val publicImagesPath: Path = rootPath.resolve("public/images")
    private val promptExampleImagesPath: Path = rootPath.resolve("public/images/prompt-example-images")

    init {
        logger.info("Initializing ImageService with storage root: $rootPath")
        logger.info("Private images path: $privateImagesPath")
        logger.info("Public images path: $publicImagesPath")
        logger.info("Prompt example images path: $promptExampleImagesPath")
        createDirectories()
    }

    fun upload(
        file: MultipartFile,
        request: CreateImageRequest,
    ): ImageDto {
        logger.debug("Starting file upload - Type: ${request.imageType}, Original filename: ${file.originalFilename}")
        validateFile(file)

        val originalFilename = file.originalFilename ?: "unknown"

        // For prompt examples, always use .webp extension
        val fileExtension = if (request.imageType == ImageType.PROMPT_EXAMPLE) ".webp" else getFileExtension(originalFilename)
        val storedFilename = "${UUID.randomUUID()}$fileExtension"

        val targetPath = getTargetPath(request.imageType)
        val filePath = targetPath.resolve(storedFilename)

        logger.info("Storing file - Target path: ${filePath.toAbsolutePath()}")

        try {
            if (request.imageType == ImageType.PROMPT_EXAMPLE) {
                // Convert to WebP for prompt examples
                logger.debug("Converting image to WebP format")
                val webpBytes = imageConversionService.convertToWebP(file.bytes)
                Files.write(filePath, webpBytes)
                logger.info("Successfully stored WebP image: ${filePath.toAbsolutePath()}")
            } else {
                // Store as-is for other types
                Files.copy(file.inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
                logger.info("Successfully stored image: ${filePath.toAbsolutePath()}")
            }
        } catch (e: IOException) {
            logger.error("Failed to store file at ${filePath.toAbsolutePath()}: ${e.message}", e)
            throw RuntimeException("Failed to store file: ${e.message}", e)
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
        val targetPath = getTargetPath(imageType)
        val filePath = targetPath.resolve(filename)

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
        val targetPath = getTargetPath(imageType)
        val filePath = targetPath.resolve(filename)

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
            findImageType(filename)
                ?: throw ResourceNotFoundException("Image with filename $filename not found")
        return getImageData(filename, imageType)
    }

    fun delete(filename: String) {
        val imageType =
            findImageType(filename)
                ?: throw ResourceNotFoundException("Image with filename $filename not found")
        delete(filename, imageType)
    }

    private fun findImageType(filename: String): ImageType? {
        val imageTypePaths =
            mapOf(
                ImageType.PRIVATE to privateImagesPath,
                ImageType.PUBLIC to publicImagesPath,
                ImageType.PROMPT_EXAMPLE to promptExampleImagesPath,
            )

        for ((imageType, path) in imageTypePaths) {
            if (Files.exists(path.resolve(filename))) {
                return imageType
            }
        }
        return null
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

    private fun getTargetPath(imageType: ImageType): Path =
        when (imageType) {
            ImageType.PUBLIC -> publicImagesPath
            ImageType.PRIVATE -> privateImagesPath
            ImageType.PROMPT_EXAMPLE -> promptExampleImagesPath
        }

    private fun createDirectories() {
        try {
            Files.createDirectories(privateImagesPath)
            logger.info("Created/verified directory: ${privateImagesPath.toAbsolutePath()}")

            Files.createDirectories(publicImagesPath)
            logger.info("Created/verified directory: ${publicImagesPath.toAbsolutePath()}")

            Files.createDirectories(promptExampleImagesPath)
            logger.info("Created/verified directory: ${promptExampleImagesPath.toAbsolutePath()}")
        } catch (e: IOException) {
            logger.error("Failed to create storage directories: ${e.message}", e)
            throw RuntimeException("Failed to create storage directories: ${e.message}", e)
        }
    }
}
