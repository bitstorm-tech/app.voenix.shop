package com.jotoai.voenix.shop.user

import com.jotoai.voenix.shop.application.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.application.ResourceNotFoundException

/**
 * Public API for the User module.
 * This interface exposes only the functions used by other modules, maintaining clean module boundaries.
 *
 * Functions used by other modules:
 * - getUserById: Used by openai, cart, order, auth modules
 * - getUserByEmail: Used by openai, order, cart, image, auth modules
 * - createUser: Used by auth module
 * - updateUser: Used by auth module
 *
 * @since 1.0
 */
interface UserService {
    /**
     * Retrieves a user by ID with optional authentication data.
     *
     * @param id The user ID
     * @param includeAuth Whether to include authentication data (passwordHash)
     * @return User DTO
     * @throws ResourceNotFoundException if user doesn't exist
     */
    fun getUserById(
        id: Long,
        includeAuth: Boolean = false,
    ): UserDto

    /**
     * Retrieves a user by email with optional authentication data.
     *
     * @param email The user email
     * @param includeAuth Whether to include authentication data (passwordHash)
     * @return User DTO or null if user doesn't exist
     */
    fun getUserByEmail(
        email: String,
        includeAuth: Boolean = false,
    ): UserDto?

    /**
     * Creates a new user.
     *
     * @param request User creation request
     * @return Created user DTO
     * @throws ResourceAlreadyExistsException if email already exists
     */
    fun createUser(request: CreateUserRequest): UserDto

    /**
     * Updates an existing user, including roles if provided.
     *
     * @param id The user ID
     * @param request User update request (includes roles)
     * @return Updated user DTO
     * @throws ResourceNotFoundException if user doesn't exist
     * @throws ResourceAlreadyExistsException if email already exists
     */
    fun updateUser(
        id: Long,
        request: UpdateUserRequest,
    ): UserDto
}
