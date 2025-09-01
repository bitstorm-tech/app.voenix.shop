package com.jotoai.voenix.shop.user.api

import com.jotoai.voenix.shop.application.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.application.ResourceNotFoundException
import com.jotoai.voenix.shop.user.api.dto.CreateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UpdateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UserAuthenticationDto
import com.jotoai.voenix.shop.user.api.dto.UserDto

/**
 * Public API for the User module.
 * This interface exposes only the functions used by other modules, maintaining clean module boundaries.
 *
 * Functions used by other modules:
 * - loadUserByEmail: Used by auth module
 * - getUserById: Used by openai, cart, order, auth modules
 * - getUserByEmail: Used by openai, order, cart, image modules
 * - existsByEmail: Used by supplier, auth modules
 * - createUser: Used by auth module
 * - updateUser: Used by auth module
 * - getUserRoles: Used by auth module
 * - setUserRoles: Used by auth module
 *
 * @since 1.0
 */
interface UserService {
    /**
     * Loads user authentication details by email for authentication purposes.
     * Returns UserAuthenticationDto to avoid circular dependencies with auth module.
     *
     * @param email The user's email address
     * @return UserAuthenticationDto or null if user doesn't exist
     */
    fun loadUserByEmail(email: String): UserAuthenticationDto?

    /**
     * Retrieves a user by ID.
     *
     * @param id The user ID
     * @return User DTO
     * @throws ResourceNotFoundException if user doesn't exist
     */
    fun getUserById(id: Long): UserDto

    /**
     * Retrieves a user by email.
     *
     * @param email The user email
     * @return User DTO
     * @throws ResourceNotFoundException if user doesn't exist
     */
    fun getUserByEmail(email: String): UserDto

    /**
     * Checks if a user exists by email.
     *
     * @param email The email to check
     * @return true if a user with the email exists, false otherwise
     */
    fun existsByEmail(email: String): Boolean

    /**
     * Creates a new user.
     *
     * @param request User creation request
     * @return Created user DTO
     * @throws ResourceAlreadyExistsException if email already exists
     */
    fun createUser(request: CreateUserRequest): UserDto

    /**
     * Updates an existing user.
     *
     * @param id The user ID
     * @param request User update request
     * @return Updated user DTO
     * @throws ResourceNotFoundException if user doesn't exist
     * @throws ResourceAlreadyExistsException if email already exists
     */
    fun updateUser(
        id: Long,
        request: UpdateUserRequest,
    ): UserDto

    /**
     * Retrieves all role names assigned to a user.
     *
     * @param userId The ID of the user
     * @return Set of role names assigned to the user
     * @throws ResourceNotFoundException if user doesn't exist
     */
    fun getUserRoles(userId: Long): Set<String>

    /**
     * Replaces all roles for a user with the provided set of role names.
     *
     * @param userId The ID of the user
     * @param roleNames Set of role names to set for the user
     * @throws ResourceNotFoundException if user doesn't exist
     * @throws ResourceNotFoundException if any role doesn't exist
     */
    fun setUserRoles(
        userId: Long,
        roleNames: Set<String>,
    )
}
