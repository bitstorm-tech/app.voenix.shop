package com.jotoai.voenix.shop.auth.internal.exception

/**
 * Exception thrown when login credentials are invalid.
 */
class InvalidCredentialsException(
    message: String = "Invalid email or password",
    cause: Throwable? = null,
) : RuntimeException(message, cause)
