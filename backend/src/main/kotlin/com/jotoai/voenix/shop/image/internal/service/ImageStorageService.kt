package com.jotoai.voenix.shop.image.internal.service

import com.jotoai.voenix.shop.application.api.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.ImageStorage
import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.image.api.dto.ImageType
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
import java.nio.file.Path
import java.util.UUID

@Service
@Transactional
class ImageStorageService(
    private val fileStorageService: FileStorageService,
    private val storagePathServiceImpl: StoragePathServiceImpl,
    private val uploadedImageRepository: UploadedImageRepository,
    private val generatedImageRepository: GeneratedImageRepository,
) : ImageStorage {
    
    companion object {
        private val logger = KotlinLogging.logger {}
        private const val ORIGINAL_SUFFIX = "_original"
        private const val GENERATED_PREFIX = "_generated_"
    }

    override fun storeFile(
        file: MultipartFile,
        imageType: ImageType,
        cropArea: CropArea?,
    ): String = fileStorageService.storeFile(file, imageType, cropArea)

    override fun storeFile(
        bytes: ByteArray,
        originalFilename: String,
        imageType: ImageType,
    ): String = fileStorageService.storeFile(bytes, originalFilename, imageType)

    override fun loadFileAsResource(filename: String, imageType: ImageType): Resource =
        fileStorageService.loadFileAsResource(filename, imageType)

    override fun loadFileAsBytes(filename: String, imageType: ImageType): ByteArray =
        fileStorageService.loadFileAsBytes(filename, imageType)

    override fun deleteFile(filename: String, imageType: ImageType): Boolean =
        fileStorageService.deleteFile(filename, imageType)

    override fun fileExists(filename: String, imageType: ImageType): Boolean =
        fileStorageService.fileExists(filename, imageType)

    override fun getImageData(filename: String, userId: Long?): Pair<ByteArray, String> =
        when {
            userId != null -> validateAccessAndGetImageData(filename, userId)
            else -> {
                val imageType = storagePathServiceImpl.findImageTypeByFilename(filename)
                    ?: throw ResourceNotFoundException("Image with filename $filename not found")
                val bytes = fileStorageService.loadFileAsBytes(filename, imageType)
                val filePath = storagePathServiceImpl.getPhysicalFilePath(imageType, filename)
                val contentType = try {
                    java.nio.file.Files.probeContentType(filePath) ?: "application/octet-stream"
                } catch (_: java.io.IOException) {
                    "application/octet-stream"
                }
                Pair(bytes, contentType)
            }
        }

    override fun serveUserImage(filename: String, userId: Long): ResponseEntity<Resource> {
        val (imageData, contentType) = validateAccessAndGetImageData(filename, userId)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"$filename\"")
            .contentType(MediaType.parseMediaType(contentType))
            .body(ByteArrayResource(imageData))
    }

    override fun getImageUrl(imageType: ImageType, filename: String): String =
        storagePathServiceImpl.getImageUrl(imageType, filename)

    override fun getPhysicalPath(imageType: ImageType): Path =
        storagePathServiceImpl.getPhysicalPath(imageType)

    override fun getPhysicalFilePath(imageType: ImageType, filename: String): Path =
        storagePathServiceImpl.getPhysicalFilePath(imageType, filename)

    private fun validateAccessAndGetImageData(filename: String, userId: Long): Pair<ByteArray, String> {
        logger.debug { "Validating access to image $filename for user $userId" }
        validateImageAccess(filename, userId)
        return fileStorageService.getUserImageData(filename, userId)
    }

    private fun validateImageAccess(filename: String, userId: Long) {
        val isOriginalImage = filename.contains(ORIGINAL_SUFFIX)
        val isGeneratedImage = filename.contains(GENERATED_PREFIX)

        when {
            isOriginalImage -> validateOriginalImageAccess(filename, userId)
            isGeneratedImage -> validateGeneratedImageAccess(filename, userId)
            else -> throw ResourceNotFoundException("Invalid image filename format")
        }
    }

    private fun validateOriginalImageAccess(filename: String, userId: Long) {
        val uuid = extractUuidFromOriginalFilename(filename)
        uploadedImageRepository.findByUserIdAndUuid(userId, uuid)
            ?: throw ResourceNotFoundException("Uploaded image not found or access denied")
        logger.debug { "Access granted to original image $filename for user $userId" }
    }

    private fun validateGeneratedImageAccess(filename: String, userId: Long) {
        val generatedImage = generatedImageRepository.findByFilename(filename)
            ?: throw ResourceNotFoundException("Generated image not found")

        if (generatedImage.userId != userId) {
            throw ResourceNotFoundException("Generated image not found or access denied")
        }
        logger.debug { "Access granted to generated image $filename for user $userId" }
    }

    private fun extractUuidFromOriginalFilename(filename: String): UUID {
        try {
            val uuidString = filename.substringBefore(ORIGINAL_SUFFIX)
            return UUID.fromString(uuidString)
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Invalid UUID format in filename: $filename" }
            throw ResourceNotFoundException("Invalid image filename format")
        }
    }
}
