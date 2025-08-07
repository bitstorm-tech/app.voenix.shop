package com.jotoai.voenix.shop.user.api

/**
 * Service interface for managing user roles.
 * This interface provides role assignment and management operations for users.
 *
 * @since 1.0
 */
interface UserRoleManagementService {
    /**
     * Assigns roles to a user by role names.
     *
     * @param userId The ID of the user to assign roles to
     * @param roleNames Set of role names to assign to the user
     * @throws com.jotoai.voenix.shop.user.api.exceptions.UserNotFoundException if user doesn't exist
     * @throws com.jotoai.voenix.shop.common.exception.ResourceNotFoundException if any role doesn't exist
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
     * @throws com.jotoai.voenix.shop.user.api.exceptions.UserNotFoundException if user doesn't exist
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
     * @throws com.jotoai.voenix.shop.user.api.exceptions.UserNotFoundException if user doesn't exist
     */
    fun getUserRoles(userId: Long): Set<String>

    /**
     * Replaces all roles for a user with the provided set of role names.
     *
     * @param userId The ID of the user
     * @param roleNames Set of role names to set for the user
     * @throws com.jotoai.voenix.shop.user.api.exceptions.UserNotFoundException if user doesn't exist
     * @throws com.jotoai.voenix.shop.common.exception.ResourceNotFoundException if any role doesn't exist
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
     * @throws com.jotoai.voenix.shop.user.api.exceptions.UserNotFoundException if user doesn't exist
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
