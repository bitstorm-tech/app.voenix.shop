package com.jotoai.voenix.shop.image.api

import com.jotoai.voenix.shop.image.api.dto.CreateImageRequest
import com.jotoai.voenix.shop.image.api.dto.ImageDto
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
    ): ImageDto

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
    ): ImageDto

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
    fun getUserUploadedImages(userId: Long): List<ImageDto>
}