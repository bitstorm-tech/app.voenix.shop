package com.jotoai.voenix.shop.image.api

import com.jotoai.voenix.shop.image.api.dto.ImageDto
import java.util.UUID

/**
 * Query service for Image module read operations.
 * This interface defines all read-only operations for image data.
 * It serves as the primary read API for other modules to access image information.
 */
interface ImageQueryService {
    /**
     * Retrieves an image by filename.
     */
    fun findImageByFilename(filename: String): ImageDto?

    /**
     * Retrieves an uploaded image by its UUID.
     */
    fun findUploadedImageByUuid(uuid: UUID): ImageDto?

    /**
     * Retrieves all uploaded images for a user.
     */
    fun findUploadedImagesByUserId(userId: Long): List<ImageDto>

    /**
     * Checks if an uploaded image exists by its UUID.
     */
    fun existsByUuid(uuid: UUID): Boolean

    /**
     * Checks if an uploaded image exists and belongs to the specified user.
     */
    fun existsByUuidAndUserId(
        uuid: UUID,
        userId: Long,
    ): Boolean

    /**
     * Checks if a generated image exists by its Long ID.
     */
    fun existsGeneratedImageById(id: Long): Boolean

    /**
     * Checks if a generated image exists by its Long ID and belongs to the specified user.
     */
    fun existsGeneratedImageByIdAndUserId(
        id: Long,
        userId: Long,
    ): Boolean

    /**
     * Validates generated image ownership for cart operations.
     * For authenticated users: Checks if the image exists and belongs to the user.
     * For anonymous users (userId null): Only checks if the image exists.
     *
     * @param imageId the ID of the generated image
     * @param userId the user ID, null for anonymous users
     * @return true if validation passes, false otherwise
     */
    fun validateGeneratedImageOwnership(
        imageId: Long,
        userId: Long?,
    ): Boolean
}
