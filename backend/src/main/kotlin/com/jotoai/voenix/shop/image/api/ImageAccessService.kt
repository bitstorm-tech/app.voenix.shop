package com.jotoai.voenix.shop.image.api

import com.jotoai.voenix.shop.image.api.dto.ImageFormat
import com.jotoai.voenix.shop.image.api.dto.ImageType
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
     * Serves an image file with proper headers and access control.
     */
    fun serveImage(
        filename: String,
        imageType: ImageType,
        format: ImageFormat? = null,
    ): ResponseEntity<Resource>

    /**
     * Serves an image for authenticated users.
     */
    fun serveUserImage(
        filename: String,
        userId: Long,
        format: ImageFormat? = null,
    ): ResponseEntity<Resource>

    /**
     * Serves a public image.
     */
    fun servePublicImage(
        filename: String,
        format: ImageFormat? = null,
    ): ResponseEntity<Resource>
}
