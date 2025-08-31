package com.jotoai.voenix.shop.image.api

import com.jotoai.voenix.shop.image.api.dto.GeneratedImageDto

/**
 * Query service for Image module read operations.
 * This interface defines read-only operations for generated image data used by other modules.
 *
 * @deprecated Use ImageService instead. This interface will be removed in a future version.
 * @see ImageService
 */
@Deprecated(
    message = "Use ImageService instead for a unified API",
    replaceWith = ReplaceWith("ImageService", "com.jotoai.voenix.shop.image.api.ImageService"),
    level = DeprecationLevel.WARNING,
)
interface ImageQueryService {
    /**
     * Checks if a generated image exists by its Long ID.
     */
    fun existsGeneratedImageById(id: Long): Boolean

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

    /**
     * Retrieves multiple generated images by their Long IDs.
     * This method performs batch loading to avoid N+1 query problems.
     *
     * @param ids list of generated image IDs
     * @return map of ID to GeneratedImageDto for found images
     */
    fun findGeneratedImagesByIds(ids: List<Long>): Map<Long, GeneratedImageDto>
}
