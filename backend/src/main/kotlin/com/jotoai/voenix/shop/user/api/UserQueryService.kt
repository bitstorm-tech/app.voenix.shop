package com.jotoai.voenix.shop.user.api

import com.jotoai.voenix.shop.user.api.dto.UserDto
import com.jotoai.voenix.shop.user.api.dto.UserSearchCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Query service for User module read operations.
 * This interface defines all read-only operations for user data.
 *
 * @since 1.0
 */
interface UserQueryService {
    /**
     * Retrieves all users.
     *
     * @return List of all active users
     */
    fun getAllUsers(): List<UserDto>

    /**
     * Retrieves all users with pagination support.
     *
     * @param pageable Pagination information
     * @return Page containing user DTOs
     */
    fun getAllUsers(pageable: Pageable): Page<UserDto>

    /**
     * Searches for users based on criteria with pagination support.
     *
     * @param criteria Search criteria to filter users
     * @param pageable Pagination information
     * @return Page containing matching user DTOs
     */
    fun searchUsers(
        criteria: UserSearchCriteria,
        pageable: Pageable,
    ): Page<UserDto>

    /**
     * Retrieves a user by ID.
     *
     * @param id The user ID
     * @return User DTO
     * @throws com.jotoai.voenix.shop.user.api.exceptions.UserNotFoundException if user doesn't exist
     */
    fun getUserById(id: Long): UserDto

    /**
     * Retrieves a user by email.
     *
     * @param email The user email
     * @return User DTO
     * @throws com.jotoai.voenix.shop.user.api.exceptions.UserNotFoundException if user doesn't exist
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
     * Gets the total count of active users.
     *
     * @return Total number of active users
     */
    fun getTotalUserCount(): Long

    /**
     * Gets users by a list of IDs.
     *
     * @param ids List of user IDs
     * @return List of user DTOs for found users
     */
    fun getUsersByIds(ids: List<Long>): List<UserDto>
}
