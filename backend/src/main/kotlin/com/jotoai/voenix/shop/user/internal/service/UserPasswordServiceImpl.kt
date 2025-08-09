package com.jotoai.voenix.shop.user.internal.service

import com.jotoai.voenix.shop.user.api.PasswordValidationResult
import com.jotoai.voenix.shop.user.api.UserPasswordService
import com.jotoai.voenix.shop.user.api.dto.UserDto
import com.jotoai.voenix.shop.user.api.exceptions.createUserNotFoundException
import com.jotoai.voenix.shop.user.internal.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of UserPasswordService for managing user passwords securely.
 * This service handles all password-related operations with proper validation and encoding.
 */
@Service
@Transactional(readOnly = true)
class UserPasswordServiceImpl(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    // Event publishing removed - handled elsewhere if needed
) : UserPasswordService {
    @Transactional
    override fun setEncodedPassword(
        userId: Long,
        encodedPassword: String,
    ): UserDto {
        val user =
            userRepository
                .findActiveById(userId)
                .orElseThrow { createUserNotFoundException("id", userId) }

        user.password = encodedPassword
        val savedUser = userRepository.save(user)
        val result = savedUser.toDto()

        // Event publishing removed - handled elsewhere if needed
        return result
    }

    override fun validatePasswordComplexity(password: String): PasswordValidationResult {
        val violations = mutableListOf<String>()

        // Minimum length check
        if (password.length < 8) {
            violations.add("Password must be at least 8 characters long")
        }

        // Maximum length check
        if (password.length > 128) {
            violations.add("Password must not exceed 128 characters")
        }

        // Character type requirements
        if (!password.any { it.isLowerCase() }) {
            violations.add("Password must contain at least one lowercase letter")
        }

        if (!password.any { it.isUpperCase() }) {
            violations.add("Password must contain at least one uppercase letter")
        }

        if (!password.any { it.isDigit() }) {
            violations.add("Password must contain at least one digit")
        }

        if (!password.any { !it.isLetterOrDigit() }) {
            violations.add("Password must contain at least one special character")
        }

        // Common password patterns to avoid
        val commonPatterns =
            listOf(
                "password",
                "123456",
                "qwerty",
                "admin",
                "user",
                "test",
            )

        val lowerPassword = password.lowercase()
        commonPatterns.forEach { pattern ->
            if (lowerPassword.contains(pattern)) {
                violations.add("Password contains common patterns that are not allowed")
                return@forEach // Only add this violation once
            }
        }

        return PasswordValidationResult(
            isValid = violations.isEmpty(),
            violations = violations,
        )
    }

    @Transactional
    override fun changePassword(
        userId: Long,
        currentPassword: String,
        newPassword: String,
    ): UserDto {
        val user =
            userRepository
                .findActiveById(userId)
                .orElseThrow { createUserNotFoundException("id", userId) }

        // Verify current password
        if (user.password == null || !passwordEncoder.matches(currentPassword, user.password)) {
            throw IllegalArgumentException("Current password is incorrect")
        }

        // Validate new password
        val validation = validatePasswordComplexity(newPassword)
        if (!validation.isValid) {
            throw IllegalArgumentException("Password validation failed: ${validation.violations.joinToString(", ")}")
        }

        // Encode and set new password
        user.password = passwordEncoder.encode(newPassword)
        val savedUser = userRepository.save(user)
        val result = savedUser.toDto()

        // Event publishing removed - handled elsewhere if needed
        return result
    }

    @Transactional
    override fun resetPassword(
        userId: Long,
        newPassword: String,
    ): UserDto {
        val user =
            userRepository
                .findActiveById(userId)
                .orElseThrow { createUserNotFoundException("id", userId) }

        // Validate new password
        val validation = validatePasswordComplexity(newPassword)
        if (!validation.isValid) {
            throw IllegalArgumentException("Password validation failed: ${validation.violations.joinToString(", ")}")
        }

        // Encode and set new password
        user.password = passwordEncoder.encode(newPassword)
        val savedUser = userRepository.save(user)
        val result = savedUser.toDto()

        // Event publishing removed - handled elsewhere if needed
        return result
    }

    override fun verifyPassword(
        userId: Long,
        rawPassword: String,
    ): Boolean {
        val user =
            userRepository
                .findActiveById(userId)
                .orElseThrow { createUserNotFoundException("id", userId) }

        return user.password != null && passwordEncoder.matches(rawPassword, user.password)
    }
}
