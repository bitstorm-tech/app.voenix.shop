package com.jotoai.voenix.shop.user

/**
 * DTO containing user authentication information.
 * This DTO is specifically designed for authentication purposes
 * to avoid circular dependencies with auth module.
 *
 * @param id The user's ID
 * @param email The user's email address
 * @param passwordHash The user's hashed password (nullable for users without passwords)
 * @param roles Set of role names assigned to the user
 * @param isActive Whether the user is active (not soft deleted)
 */
data class UserAuthenticationDto(
    val id: Long,
    val email: String,
    val passwordHash: String? = null,
    val roles: Set<String> = emptySet(),
    val isActive: Boolean = true,
)
