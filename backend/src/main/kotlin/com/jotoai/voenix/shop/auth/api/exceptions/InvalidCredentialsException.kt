package com.jotoai.voenix.shop.auth.api.exceptions

/**
 * Exception thrown when login credentials are invalid.
 */
class InvalidCredentialsException(
    message: String = "Invalid email or password",
    cause: Throwable? = null,
) : AuthenticationException(message, cause)
