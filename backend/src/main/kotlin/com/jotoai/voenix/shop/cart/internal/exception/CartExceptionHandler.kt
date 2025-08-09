package com.jotoai.voenix.shop.cart.internal.exception

import com.jotoai.voenix.shop.cart.api.exceptions.CartItemNotFoundException
import com.jotoai.voenix.shop.cart.api.exceptions.CartNotFoundException
import com.jotoai.voenix.shop.cart.api.exceptions.CartOperationException
import com.jotoai.voenix.shop.common.dto.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class CartExceptionHandler {
    @ExceptionHandler(CartNotFoundException::class)
    fun handleCartNotFoundException(ex: CartNotFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.NOT_FOUND.value(),
                error = "Cart Not Found",
                message = ex.message ?: "Cart not found",
                path = "",
            )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(CartItemNotFoundException::class)
    fun handleCartItemNotFoundException(ex: CartItemNotFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.NOT_FOUND.value(),
                error = "Cart Item Not Found",
                message = ex.message ?: "Cart item not found",
                path = "",
            )

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(CartOperationException::class)
    fun handleCartOperationException(ex: CartOperationException): ResponseEntity<ErrorResponse> {
        val errorResponse =
            ErrorResponse(
                timestamp = LocalDateTime.now(),
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Cart Operation Failed",
                message = ex.message ?: "Cart operation failed",
                path = "",
            )

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
}
