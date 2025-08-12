package com.jotoai.voenix.shop.image.api

import com.jotoai.voenix.shop.image.api.dto.CreateImageRequest
import com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageDto
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.dto.UpdateGeneratedImageRequest
import com.jotoai.voenix.shop.image.api.dto.UploadedImageDto
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

/**
 * Main facade for Image module operations.
 * This interface defines all administrative operations for managing images.
 */
interface ImageFacade {
    /**
     * Creates a new uploaded image from multipart file.
     */
    fun createUploadedImage(
        file: MultipartFile,
        userId: Long,
    ): UploadedImageDto

    /**
     * Creates a new uploaded image from multipart file with specified image type.
     */
    fun createUploadedImage(
        file: MultipartFile,
        userId: Long,
        imageType: ImageType,
    ): UploadedImageDto

    /**
     * Creates an image from a create request.
     */
    fun createImage(request: CreateImageRequest): ImageDto

    /**
     * Retrieves an uploaded image by its UUID.
     */
    fun getUploadedImageByUuid(
        uuid: UUID,
        userId: Long,
    ): UploadedImageDto

    /**
     * Deletes an uploaded image.
     */
    fun deleteUploadedImage(
        uuid: UUID,
        userId: Long,
    )

    /**
     * Retrieves all uploaded images for a user.
     */
    fun getUserUploadedImages(userId: Long): List<UploadedImageDto>

    // Generated images management

    /**
     * Retrieves a generated image by its UUID.
     */
    fun getGeneratedImageByUuid(
        uuid: UUID,
        userId: Long? = null,
    ): GeneratedImageDto

    /**
     * Updates a generated image.
     */
    fun updateGeneratedImage(
        uuid: UUID,
        updateRequest: UpdateGeneratedImageRequest,
        userId: Long? = null,
    ): GeneratedImageDto

    /**
     * Deletes a generated image.
     */
    fun deleteGeneratedImage(
        uuid: UUID,
        userId: Long? = null,
    )

    /**
     * Retrieves all generated images for a user.
     */
    fun getUserGeneratedImages(userId: Long): List<GeneratedImageDto>
}
