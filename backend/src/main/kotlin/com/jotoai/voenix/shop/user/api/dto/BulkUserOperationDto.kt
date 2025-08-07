package com.jotoai.voenix.shop.user.api.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

/**
 * Request for bulk user creation operations.
 *
 * @param users List of user creation requests
 */
data class BulkCreateUsersRequest(
    @field:NotEmpty(message = "Users list cannot be empty")
    @field:Valid
    val users: List<CreateUserRequest>,
)

/**
 * Request for bulk user update operations.
 *
 * @param updates List of user update operations with their IDs
 */
data class BulkUpdateUsersRequest(
    @field:NotEmpty(message = "Updates list cannot be empty")
    @field:Valid
    val updates: List<UserUpdateOperation>,
)

/**
 * Individual user update operation for bulk updates.
 *
 * @param id The ID of the user to update
 * @param request The update request
 */
data class UserUpdateOperation(
    val id: Long,
    @field:Valid
    val request: UpdateUserRequest,
)

/**
 * Request for bulk user deletion operations.
 *
 * @param userIds List of user IDs to delete
 */
data class BulkDeleteUsersRequest(
    @field:NotEmpty(message = "User IDs list cannot be empty")
    val userIds: List<Long>,
)

/**
 * Result of a bulk operation.
 *
 * @param T The type of the result data
 * @param successful List of successful operations
 * @param failed List of failed operations with their errors
 */
data class BulkOperationResult<T>(
    val successful: List<T>,
    val failed: List<BulkOperationError>,
) {
    val totalProcessed: Int get() = successful.size + failed.size
    val successCount: Int get() = successful.size
    val failureCount: Int get() = failed.size
    val hasFailures: Boolean get() = failed.isNotEmpty()
}

/**
 * Represents a failed operation in a bulk request.
 *
 * @param index The index of the failed operation in the original request
 * @param identifier An identifier for the failed operation (e.g., user ID or email)
 * @param error The error message
 */
data class BulkOperationError(
    val index: Int,
    val identifier: String,
    val error: String,
)
