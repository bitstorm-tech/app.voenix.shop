package com.jotoai.voenix.shop.auth.api.exceptions

/**
 * Exception thrown when user registration fails.
 */
class UserRegistrationException(
    message: String,
    cause: Throwable? = null,
) : AuthenticationException(message, cause)
