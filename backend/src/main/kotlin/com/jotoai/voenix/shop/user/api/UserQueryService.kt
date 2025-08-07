package com.jotoai.voenix.shop.user.api

import com.jotoai.voenix.shop.user.api.dto.UserDto

/**
 * Query service for User module read operations.
 * This interface defines all read-only operations for user data.
 */
interface UserQueryService {
    /**
     * Retrieves all users.
     */
    fun getAllUsers(): List<UserDto>

    /**
     * Retrieves a user by ID.
     */
    fun getUserById(id: Long): UserDto

    /**
     * Retrieves a user by email.
     */
    fun getUserByEmail(email: String): UserDto

    /**
     * Checks if a user exists by email.
     */
    fun existsByEmail(email: String): Boolean
}
