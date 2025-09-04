package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.application.ResourceNotFoundException
import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.image.ImageType
import io.github.oshai.kotlinlogging.KLogger
import org.springframework.data.repository.CrudRepository
import java.io.IOException

/**
 * Shared, tiny helpers to remove repeated boilerplate in services.
 */
object ServiceUtils {
    /**
     * Deletes an image file, logging a warning on failure without throwing.
     */
    fun safeDeleteImage(
        imageService: ImageService,
        filename: String,
        type: ImageType,
        logger: KLogger,
    ) {
        try {
            imageService.delete(filename, type)
        } catch (e: IOException) {
            logger.warn(e) { "Failed to delete image '$filename' of type '$type'" }
        }
    }
}

/**
 * Repository helper to fetch or throw a standardized not-found exception.
 */
fun <T : Any, ID : Any> CrudRepository<T, ID>.getOrNotFound(
    id: ID,
    resource: String,
): T = this.findById(id).orElseThrow { ResourceNotFoundException(resource, "id", id) }
