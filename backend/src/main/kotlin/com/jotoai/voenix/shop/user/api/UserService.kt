package com.jotoai.voenix.shop.user.api

import com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.user.api.dto.BulkCreateUsersRequest
import com.jotoai.voenix.shop.user.api.dto.BulkDeleteUsersRequest
import com.jotoai.voenix.shop.user.api.dto.BulkOperationResult
import com.jotoai.voenix.shop.user.api.dto.BulkUpdateUsersRequest
import com.jotoai.voenix.shop.user.api.dto.CreateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UpdateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UserAuthenticationDto
import com.jotoai.voenix.shop.user.api.dto.UserDto
import com.jotoai.voenix.shop.user.api.dto.UserSearchCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * Data class representing password validation result.
 *
 * @param isValid True if the password meets all requirements
 * @param violations List of specific validation violations
 */
data class PasswordValidationResult(
    val isValid: Boolean,
    val violations: List<String> = emptyList(),
)

/**
 * Unified service for all User module operations.
 * This service combines authentication, query, command, password management, and role management operations.
 *
 * This replaces the previous CQRS-style separation into multiple services:
 * - UserAuthenticationService
 * - UserFacade
 * - UserPasswordService
 * - UserQueryService
 * - UserRoleManagementService
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
     * Updates user authentication-related fields (password, OTP).
     *
     * @param id The user ID
     * @param password The new password (optional)
     * @param oneTimePassword The new one-time password (optional)
     * @return Updated user DTO
     * @throws ResourceNotFoundException if user doesn't exist
     */
    fun updateUserAuthFields(
        id: Long,
        password: String? = null,
        oneTimePassword: String? = null,
    ): UserDto

    

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
     * Soft deletes a user (marks as deleted without removing from database).
     *
     * @param id The user ID
     * @throws ResourceNotFoundException if user doesn't exist
     */
    fun softDeleteUser(id: Long)

    /**
     * Restores a soft-deleted user.
     *
     * @param id The user ID
     * @return Restored user DTO
     * @throws ResourceNotFoundException if user doesn't exist
     */
    fun restoreUser(id: Long): UserDto

    /**
     * Creates multiple users in a single operation.
     *
     * @param request Bulk create request
     * @return Result containing successful creations and any failures
     */
    fun bulkCreateUsers(request: BulkCreateUsersRequest): BulkOperationResult<UserDto>

    /**
     * Updates multiple users in a single operation.
     *
     * @param request Bulk update request
     * @return Result containing successful updates and any failures
     */
    fun bulkUpdateUsers(request: BulkUpdateUsersRequest): BulkOperationResult<UserDto>

    /**
     * Soft deletes multiple users in a single operation.
     *
     * @param request Bulk delete request
     * @return Result containing successful deletions and any failures
     */
    fun bulkDeleteUsers(request: BulkDeleteUsersRequest): BulkOperationResult<Long>

    /**
     * Sets an already encoded password for a user.
     * This method should only be used with passwords that have already been hashed/encoded.
     *
     * @param userId The ID of the user
     * @param encodedPassword The already encoded password
     * @return Updated user DTO
     * @throws ResourceNotFoundException if user doesn't exist
     */
    fun setEncodedPassword(
        userId: Long,
        encodedPassword: String,
    ): UserDto

    /**
     * Validates password complexity according to system requirements.
     *
     * @param password The raw password to validate
     * @return PasswordValidationResult containing validation status and any violations
     */
    fun validatePasswordComplexity(password: String): PasswordValidationResult

    /**
     * Changes a user's password after validating the current password.
     *
     * @param userId The ID of the user
     * @param currentPassword The current password (raw)
     * @param newPassword The new password (raw)
     * @return Updated user DTO
     * @throws ResourceNotFoundException if user doesn't exist
     * @throws IllegalArgumentException if current password is incorrect or new password doesn't meet requirements
     */
    fun changePassword(
        userId: Long,
        currentPassword: String,
        newPassword: String,
    ): UserDto

    /**
     * Resets a user's password (admin function).
     * This method should only be used by administrators and bypasses current password validation.
     *
     * @param userId The ID of the user
     * @param newPassword The new password (raw)
     * @return Updated user DTO
     * @throws ResourceNotFoundException if user doesn't exist
     * @throws IllegalArgumentException if new password doesn't meet requirements
     */
    fun resetPassword(
        userId: Long,
        newPassword: String,
    ): UserDto

    /**
     * Verifies if a raw password matches the user's current password.
     *
     * @param userId The ID of the user
     * @param rawPassword The raw password to verify
     * @return true if the password matches, false otherwise
     * @throws ResourceNotFoundException if user doesn't exist
     */
    fun verifyPassword(
        userId: Long,
        rawPassword: String,
    ): Boolean

    /**
     * Assigns roles to a user by role names.
     *
     * @param userId The ID of the user to assign roles to
     * @param roleNames Set of role names to assign to the user
     * @throws ResourceNotFoundException if user doesn't exist
     * @throws ResourceNotFoundException if any role doesn't exist
     */
    fun assignRoles(
        userId: Long,
        roleNames: Set<String>,
    )

    /**
     * Removes roles from a user by role names.
     *
     * @param userId The ID of the user to remove roles from
     * @param roleNames Set of role names to remove from the user
     * @throws ResourceNotFoundException if user doesn't exist
     */
    fun removeRoles(
        userId: Long,
        roleNames: Set<String>,
    )

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

    /**
     * Checks if a user has a specific role.
     *
     * @param userId The ID of the user
     * @param roleName The name of the role to check
     * @return true if the user has the role, false otherwise
     * @throws ResourceNotFoundException if user doesn't exist
     */
    fun userHasRole(
        userId: Long,
        roleName: String,
    ): Boolean

    /**
     * Gets all available role names in the system.
     *
     * @return Set of all role names
     */
    fun getAllRoleNames(): Set<String>
}
