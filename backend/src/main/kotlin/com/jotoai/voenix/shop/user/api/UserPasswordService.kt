package com.jotoai.voenix.shop.user.api

import com.jotoai.voenix.shop.user.api.dto.UserDto

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
 * Service interface for managing user passwords securely.
 * This interface provides password security operations including validation and encoding.
 *
 * @since 1.0
 */
interface UserPasswordService {
    /**
     * Sets an already encoded password for a user.
     * This method should only be used with passwords that have already been hashed/encoded.
     *
     * @param userId The ID of the user
     * @param encodedPassword The already encoded password
     * @return Updated user DTO
     * @throws com.jotoai.voenix.shop.user.api.exceptions.UserNotFoundException if user doesn't exist
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
     * @throws com.jotoai.voenix.shop.user.api.exceptions.UserNotFoundException if user doesn't exist
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
     * @throws com.jotoai.voenix.shop.user.api.exceptions.UserNotFoundException if user doesn't exist
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
     * @throws com.jotoai.voenix.shop.user.api.exceptions.UserNotFoundException if user doesn't exist
     */
    fun verifyPassword(
        userId: Long,
        rawPassword: String,
    ): Boolean
}
