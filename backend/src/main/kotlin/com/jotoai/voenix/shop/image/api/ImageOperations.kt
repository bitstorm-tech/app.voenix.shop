package com.jotoai.voenix.shop.image.api

import com.jotoai.voenix.shop.image.api.dto.CropArea
import com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.UpdateGeneratedImageRequest
import com.jotoai.voenix.shop.image.api.dto.UploadedImageDto
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.UUID

/**
 * Core image operations interface.
 * Handles CRUD operations for both uploaded and generated images.
 */
interface ImageOperations {
    fun createUploadedImage(
        file: MultipartFile,
        userId: Long,
        imageType: ImageType = ImageType.PRIVATE,
        cropArea: CropArea? = null,
    ): UploadedImageDto

    fun getUploadedImageByUuid(
        uuid: UUID,
        userId: Long,
    ): UploadedImageDto

    fun getUserUploadedImages(userId: Long): List<UploadedImageDto>

    fun storeGeneratedImage(
        imageBytes: ByteArray,
        uploadedImageId: Long,
        promptId: Long,
        generationNumber: Int,
        ipAddress: String? = null,
    ): GeneratedImageDto

    fun storePublicGeneratedImage(
        imageBytes: ByteArray,
        promptId: Long,
        ipAddress: String,
        generationNumber: Int,
    ): GeneratedImageDto

    fun getGeneratedImageByUuid(
        uuid: UUID,
        userId: Long? = null,
    ): GeneratedImageDto

    fun updateGeneratedImage(
        uuid: UUID,
        updateRequest: UpdateGeneratedImageRequest,
        userId: Long? = null,
    ): GeneratedImageDto

    fun deleteGeneratedImage(
        uuid: UUID,
        userId: Long? = null,
    )

    fun getUserGeneratedImages(userId: Long): List<GeneratedImageDto>

    fun countGeneratedImagesForIpAfter(
        ipAddress: String,
        after: LocalDateTime,
    ): Long

    fun countGeneratedImagesForUserAfter(
        userId: Long,
        after: LocalDateTime,
    ): Long

    fun validateImageFile(file: MultipartFile)
}
