package com.jotoai.voenix.shop.image.api

import com.jotoai.voenix.shop.image.api.dto.ImageFormat
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity

/**
 * Service for image access and serving operations.
 * This interface defines operations for serving images to clients with proper access control.
 */
interface ImageAccessService {
    /**
     * Returns raw image bytes and content type for internal consumers.
     */
    fun getImageData(
        filename: String,
        userId: Long? = null,
    ): Pair<ByteArray, String>

    /**
     * Serves an image for authenticated users.
     */
    fun serveUserImage(
        filename: String,
        userId: Long,
        format: ImageFormat? = null,
    ): ResponseEntity<Resource>
}
