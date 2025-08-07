package com.jotoai.voenix.shop.user.api

import com.jotoai.voenix.shop.user.api.dto.BulkCreateUsersRequest
import com.jotoai.voenix.shop.user.api.dto.BulkDeleteUsersRequest
import com.jotoai.voenix.shop.user.api.dto.BulkOperationResult
import com.jotoai.voenix.shop.user.api.dto.BulkUpdateUsersRequest
import com.jotoai.voenix.shop.user.api.dto.CreateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UpdateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UserDto

/**
 * Main facade for User module operations.
 * This interface defines all administrative operations for managing users.
 *
 * @since 1.0
 */
interface UserFacade {
    /**
     * Creates a new user.
     *
     * @param request User creation request
     * @return Created user DTO
     * @throws com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException if email already exists
     */
    fun createUser(request: CreateUserRequest): UserDto

    /**
     * Updates an existing user.
     *
     * @param id The user ID
     * @param request User update request
     * @return Updated user DTO
     * @throws com.jotoai.voenix.shop.user.api.exceptions.UserNotFoundException if user doesn't exist
     * @throws com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException if email already exists
     */
    fun updateUser(
        id: Long,
        request: UpdateUserRequest,
    ): UserDto

    /**
     * Soft deletes a user (marks as deleted without removing from database).
     *
     * @param id The user ID
     * @throws com.jotoai.voenix.shop.user.api.exceptions.UserNotFoundException if user doesn't exist
     */
    fun softDeleteUser(id: Long)

    /**
     * Hard deletes a user (permanently removes from database).
     *
     * @param id The user ID
     * @throws com.jotoai.voenix.shop.user.api.exceptions.UserNotFoundException if user doesn't exist
     */
    fun deleteUser(id: Long)

    /**
     * Restores a soft-deleted user.
     *
     * @param id The user ID
     * @return Restored user DTO
     * @throws com.jotoai.voenix.shop.user.api.exceptions.UserNotFoundException if user doesn't exist
     */
    fun restoreUser(id: Long): UserDto

    // Bulk Operations

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
}
