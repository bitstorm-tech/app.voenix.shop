package com.jotoai.voenix.shop.application.internal.exception

import com.jotoai.voenix.shop.application.ErrorResponse
import com.jotoai.voenix.shop.application.BadRequestException
import com.jotoai.voenix.shop.application.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.application.ResourceNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
@Order(100) // Lower priority to let module-specific handlers take precedence
class CommonExceptionHandler {
    private val log = KotlinLogging.logger {}

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.NOT_FOUND.value(),
                error = "Not Found",
                message = ex.message ?: "Resource not found",
                path = "",
            )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(ResourceAlreadyExistsException::class)
    fun handleResourceAlreadyExistsException(ex: ResourceAlreadyExistsException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.CONFLICT.value(),
                error = "Conflict",
                message = ex.message ?: "Resource already exists",
                path = "",
            )

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequestException(ex: BadRequestException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Bad Request",
                message = ex.message ?: "Invalid request",
                path = "",
            )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = mutableMapOf<String, String>()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName =
                when (error) {
                    is FieldError -> error.field
                    else -> error.objectName
                }
            val errorMessage = error.defaultMessage ?: "Invalid value"
            errors[fieldName] = errorMessage
        }

        val errorResponse =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Validation Failed",
                message = "Invalid input parameters",
                path = "",
                validationErrors = errors,
            )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGlobalException(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error(ex) { "Unexpected error occurred" }

        val errorResponse =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "Internal Server Error",
                message = "An unexpected error occurred",
                path = "",
            )

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}
