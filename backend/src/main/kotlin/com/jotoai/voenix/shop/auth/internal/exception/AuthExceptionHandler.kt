package com.jotoai.voenix.shop.auth.internal.exception

import com.jotoai.voenix.shop.application.api.dto.ErrorResponse
import com.jotoai.voenix.shop.auth.api.exceptions.InvalidCredentialsException
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
@Order(1) // Higher priority than common handler
class AuthExceptionHandler {
    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.UNAUTHORIZED.value(),
                error = "Unauthorized",
                message = ex.message ?: "Invalid email or password",
                path = "",
            )

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }
}
