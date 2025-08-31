package com.jotoai.voenix.shop.image.internal.exception

import com.jotoai.voenix.shop.application.api.dto.ErrorResponse
import com.jotoai.voenix.shop.image.ImageException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
@Order(1) // Higher priority than common handler
class ImageExceptionHandler {
    private val log = KotlinLogging.logger {}

    @ExceptionHandler(ImageException::class)
    fun handleImageException(ex: ImageException): ResponseEntity<ErrorResponse> {
        val (status, errorType) =
            when (ex) {
                is ImageException.NotFound -> {
                    log.warn { "Image not found: ${ex.message}" }
                    HttpStatus.NOT_FOUND to "Not Found"
                }
                is ImageException.AccessDenied -> {
                    log.warn { "Access denied to image: ${ex.message}" }
                    HttpStatus.FORBIDDEN to "Forbidden"
                }
                is ImageException.Processing -> {
                    log.error(ex) { "Image processing error: ${ex.message}" }
                    HttpStatus.INTERNAL_SERVER_ERROR to "Processing Error"
                }
                is ImageException.Storage -> {
                    log.error(ex) { "Image storage error: ${ex.message}" }
                    HttpStatus.INTERNAL_SERVER_ERROR to "Storage Error"
                }
            }

        return ResponseEntity.status(status).body(
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = status.value(),
                error = errorType,
                message = ex.message ?: "Image operation failed",
                path = "",
            ),
        )
    }

    @ExceptionHandler(InternalImageException::class)
    fun handleInternalImageException(ex: InternalImageException): ResponseEntity<ErrorResponse> {
        val (status, errorType) =
            when (ex) {
                is InternalImageException.QuotaExceeded -> {
                    log.warn { "Image quota exceeded: ${ex.message}" }
                    HttpStatus.TOO_MANY_REQUESTS to "Quota Exceeded"
                }
                is InternalImageException.StorageConfiguration -> {
                    log.error(ex) { "Storage configuration error: ${ex.message}" }
                    HttpStatus.SERVICE_UNAVAILABLE to "Configuration Error"
                }
            }

        return ResponseEntity.status(status).body(
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = status.value(),
                error = errorType,
                message = ex.message ?: "Image service error",
                path = "",
            ),
        )
    }
}
