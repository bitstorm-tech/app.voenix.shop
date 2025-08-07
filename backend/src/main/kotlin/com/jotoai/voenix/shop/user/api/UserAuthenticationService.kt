package com.jotoai.voenix.shop.user.api

import com.jotoai.voenix.shop.auth.dto.CustomUserDetails
import com.jotoai.voenix.shop.user.api.dto.UserDto

/**
 * Authentication service for User module auth-specific operations.
 * This interface defines operations specifically needed by the auth module
 * for user authentication and authorization.
 */
interface UserAuthenticationService {
    /**
     * Loads user details by email for authentication purposes.
     * Returns CustomUserDetails suitable for Spring Security.
     */
    fun loadUserByEmail(email: String): CustomUserDetails?

    /**
     * Updates user authentication-related fields (password, OTP).
     */
    fun updateUserAuthFields(
        id: Long,
        password: String? = null,
        oneTimePassword: String? = null,
    ): UserDto
}
