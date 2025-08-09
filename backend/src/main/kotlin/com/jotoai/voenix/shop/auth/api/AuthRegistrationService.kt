package com.jotoai.voenix.shop.auth.api

import com.jotoai.voenix.shop.user.api.dto.UserDto

/**
 * Service for user registration operations.
 */
interface AuthRegistrationService {
    /**
     * Creates a new user with the specified details and roles.
     */
    fun createUser(
        email: String,
        password: String? = null,
        firstName: String? = null,
        lastName: String? = null,
        phoneNumber: String? = null,
        roleNames: Set<String> = setOf("USER"),
    ): UserDto

    /**
     * Updates an existing user's details.
     */
    fun updateUser(
        userId: Long,
        firstName: String? = null,
        lastName: String? = null,
        phoneNumber: String? = null,
    ): UserDto
}
