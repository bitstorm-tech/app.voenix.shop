package com.jotoai.voenix.shop.user.api

import com.jotoai.voenix.shop.user.api.dto.CreateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UpdateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UserDto

/**
 * Main facade for User module operations.
 * This interface defines all administrative operations for managing users.
 */
interface UserFacade {
    /**
     * Creates a new user.
     */
    fun createUser(request: CreateUserRequest): UserDto

    /**
     * Updates an existing user.
     */
    fun updateUser(
        id: Long,
        request: UpdateUserRequest,
    ): UserDto

    /**
     * Deletes a user.
     */
    fun deleteUser(id: Long)
}
