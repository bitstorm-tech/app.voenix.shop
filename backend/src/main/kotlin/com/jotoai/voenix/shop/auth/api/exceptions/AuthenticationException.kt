package com.jotoai.voenix.shop.auth.api.exceptions

/**
 * Base exception for authentication-related errors.
 */
open class AuthenticationException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
