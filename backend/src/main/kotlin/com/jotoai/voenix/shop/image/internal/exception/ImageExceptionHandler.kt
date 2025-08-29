package com.jotoai.voenix.shop.image.internal.exception

import com.jotoai.voenix.shop.common.api.dto.ErrorResponse
import com.jotoai.voenix.shop.image.api.exceptions.ImageAccessDeniedException
import com.jotoai.voenix.shop.image.api.exceptions.ImageNotFoundException
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

    @ExceptionHandler(ImageNotFoundException::class)
    fun handleImageNotFoundException(ex: ImageNotFoundException): ResponseEntity<ErrorResponse> {
        log.warn { "Image not found: ${ex.message}" }

        val errorResponse =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.NOT_FOUND.value(),
                error = "Not Found",
                message = ex.message ?: "Generated image not found",
                path = "",
            )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(ImageAccessDeniedException::class)
    fun handleImageAccessDeniedException(ex: ImageAccessDeniedException): ResponseEntity<ErrorResponse> {
        log.warn {
            "Access denied to image: userId=${ex.userId}, resourceId=${ex.resourceId}, message=${ex.message}"
        }

        val errorResponse =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.FORBIDDEN.value(),
                error = "Forbidden",
                message = ex.message ?: "You don't have permission to access this image",
                path = "",
            )

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }
}
