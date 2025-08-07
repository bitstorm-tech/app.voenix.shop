package com.jotoai.voenix.shop.user.api

import com.jotoai.voenix.shop.user.api.dto.UserAuthenticationDto
import com.jotoai.voenix.shop.user.api.dto.UserDto

/**
 * Authentication service for User module auth-specific operations.
 * This interface defines operations specifically needed by the auth module
 * for user authentication and authorization.
 *
 * @since 1.0
 */
interface UserAuthenticationService {
    /**
     * Loads user authentication details by email for authentication purposes.
     * Returns UserAuthenticationDto to avoid circular dependencies with auth module.
     *
     * @param email The user's email address
     * @return UserAuthenticationDto or null if user doesn't exist
     */
    fun loadUserByEmail(email: String): UserAuthenticationDto?

    /**
     * Updates user authentication-related fields (password, OTP).
     *
     * @param id The user ID
     * @param password The new password (optional)
     * @param oneTimePassword The new one-time password (optional)
     * @return Updated user DTO
     * @throws com.jotoai.voenix.shop.user.api.exceptions.UserNotFoundException if user doesn't exist
     */
    fun updateUserAuthFields(
        id: Long,
        password: String? = null,
        oneTimePassword: String? = null,
    ): UserDto
}
