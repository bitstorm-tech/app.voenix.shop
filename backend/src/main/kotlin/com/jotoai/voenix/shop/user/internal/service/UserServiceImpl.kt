package com.jotoai.voenix.shop.user.internal.service

import com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.user.api.PasswordValidationResult
import com.jotoai.voenix.shop.user.api.UserService
import com.jotoai.voenix.shop.user.api.dto.BulkCreateUsersRequest
import com.jotoai.voenix.shop.user.api.dto.BulkDeleteUsersRequest
import com.jotoai.voenix.shop.user.api.dto.BulkOperationError
import com.jotoai.voenix.shop.user.api.dto.BulkOperationResult
import com.jotoai.voenix.shop.user.api.dto.BulkUpdateUsersRequest
import com.jotoai.voenix.shop.user.api.dto.CreateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UpdateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UserAuthenticationDto
import com.jotoai.voenix.shop.user.api.dto.UserDto
import com.jotoai.voenix.shop.user.api.dto.UserSearchCriteria
import com.jotoai.voenix.shop.user.api.exceptions.createUserNotFoundException
import com.jotoai.voenix.shop.user.internal.entity.User
import com.jotoai.voenix.shop.user.internal.repository.RoleRepository
import com.jotoai.voenix.shop.user.internal.repository.UserRepository
import com.jotoai.voenix.shop.user.internal.repository.UserSpecifications
import java.time.OffsetDateTime
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Unified implementation of UserService that combines all user-related operations.
 *
 * This implementation combines functionality from the following services:
 * - UserAuthServiceImpl (authentication operations)
 * - UserCommandService (command operations / user management)
 * - UserPasswordServiceImpl (password management)
 * - UserQueryServiceImpl (query operations)
 * - UserRoleServiceImpl (role management)
 */
@Service
@Transactional(readOnly = true)
@Suppress("TooManyFunctions")
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
) : UserService {
    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
        private const val MAX_PASSWORD_LENGTH = 128
    }

    override fun loadUserByEmail(email: String): UserAuthenticationDto? {
        val user = userRepository.findActiveByEmail(email).orElse(null) ?: return null

        return UserAuthenticationDto(
            id = user.id!!,
            email = user.email,
            passwordHash = user.password,
            roles = user.roles.map { it.name }.toSet(),
            isActive = user.isActive(),
        )
    }

    @Transactional
    override fun updateUserAuthFields(
        id: Long,
        password: String?,
        oneTimePassword: String?,
    ): UserDto {
        val user =
            userRepository
                .findActiveById(id)
                .orElseThrow { createUserNotFoundException("id", id) }

        var updated = false

        password?.let {
            user.password = passwordEncoder.encode(it)
            updated = true
        }

        oneTimePassword?.let {
            user.oneTimePassword = it
            user.oneTimePasswordCreatedAt = OffsetDateTime.now()
            updated = true
        }

        if (updated) {
            val savedUser = userRepository.save(user)
            val result = savedUser.toDto()

            return result
        }

        return user.toDto()
    }

    override fun searchUsers(
        criteria: UserSearchCriteria,
        pageable: Pageable,
    ): Page<UserDto> {
        val spec = UserSpecifications.fromCriteria(criteria)
        return userRepository.findAll(spec, pageable).map { it.toDto() }
    }

    override fun getUserById(id: Long): UserDto =
        userRepository
            .findActiveById(id)
            .map { it.toDto() }
            .orElseThrow { createUserNotFoundException("id", id) }

    override fun getUserByEmail(email: String): UserDto =
        userRepository
            .findActiveByEmail(email)
            .map { it.toDto() }
            .orElseThrow { createUserNotFoundException("email", email) }

    override fun existsByEmail(email: String): Boolean = userRepository.existsActiveByEmail(email)

    override fun getTotalUserCount(): Long = userRepository.countActive()

    override fun getUsersByIds(ids: List<Long>): List<UserDto> = userRepository.findActiveByIds(ids).map { it.toDto() }

    @Transactional
    override fun createUser(request: CreateUserRequest): UserDto {
        // Validate email format
        require(isValidEmail(request.email)) {
            "Invalid email format: ${request.email}"
        }

        if (userRepository.existsActiveByEmail(request.email)) {
            throw ResourceAlreadyExistsException("User", "email", request.email)
        }

        val user =
            User(
                email = request.email,
                firstName = request.firstName,
                lastName = request.lastName,
                phoneNumber = request.phoneNumber,
                password = request.password?.let { passwordEncoder.encode(it) },
            )

        val savedUser = userRepository.save(user)
        val result = savedUser.toDto()

        return result
    }

    @Transactional
    override fun updateUser(
        id: Long,
        request: UpdateUserRequest,
    ): UserDto {
        val user =
            userRepository
                .findActiveById(id)
                .orElseThrow { createUserNotFoundException("id", id) }

        // Check email uniqueness if email is being updated
        request.email?.let { email ->
            if (email != user.email && userRepository.existsActiveByEmailAndIdNot(email, id)) {
                throw ResourceAlreadyExistsException("User", "email", email)
            }
            user.email = email
        }

        request.firstName?.let { user.firstName = it }
        request.lastName?.let { user.lastName = it }
        request.phoneNumber?.let { user.phoneNumber = it }
        request.password?.let { user.password = passwordEncoder.encode(it) }
        request.oneTimePassword?.let { user.oneTimePassword = it }

        val updatedUser = userRepository.save(user)
        val result = updatedUser.toDto()

        return result
    }

    @Transactional
    override fun softDeleteUser(id: Long) {
        val user =
            userRepository
                .findActiveById(id)
                .orElseThrow { createUserNotFoundException("id", id) }

        user.markAsDeleted()
    }

    

    @Transactional
    override fun restoreUser(id: Long): UserDto {
        val user =
            userRepository
                .findById(id) // Find even soft-deleted users
                .orElseThrow { createUserNotFoundException("id", id) }

        require(!user.isActive()) {
            "User with ID $id is not deleted and cannot be restored"
        }

        user.restore()
        val restoredUser = userRepository.save(user)
        val result = restoredUser.toDto()
        return result
    }

    @Transactional
    override fun bulkCreateUsers(request: BulkCreateUsersRequest): BulkOperationResult<UserDto> {
        val successful = mutableListOf<UserDto>()
        val failed = mutableListOf<BulkOperationError>()
        val usersToCreate = mutableListOf<User>()

        // Validate all users first
        request.users.forEachIndexed { index, userRequest ->
            try {
                // Validate email format
                require(isValidEmail(userRequest.email)) {
                    "Invalid email format: ${userRequest.email}"
                }

                if (userRepository.existsActiveByEmail(userRequest.email)) {
                    throw ResourceAlreadyExistsException("User", "email", userRequest.email)
                }

                val user =
                    User(
                        email = userRequest.email,
                        firstName = userRequest.firstName,
                        lastName = userRequest.lastName,
                        phoneNumber = userRequest.phoneNumber,
                        password = userRequest.password?.let { passwordEncoder.encode(it) },
                    )
                usersToCreate.add(user)
            } catch (e: ResourceAlreadyExistsException) {
                failed.add(
                    BulkOperationError(
                        index = index,
                        identifier = userRequest.email,
                        error = e.message ?: "Resource already exists",
                    ),
                )
            } catch (e: DataIntegrityViolationException) {
                failed.add(
                    BulkOperationError(
                        index = index,
                        identifier = userRequest.email,
                        error = "Data integrity violation: ${e.message}",
                    ),
                )
            } catch (e: IllegalArgumentException) {
                failed.add(
                    BulkOperationError(
                        index = index,
                        identifier = userRequest.email,
                        error = e.message ?: "Invalid input",
                    ),
                )
            }
        }

        // Batch save all valid users
        if (usersToCreate.isNotEmpty()) {
            val savedUsers = userRepository.saveAll(usersToCreate)
            savedUsers.forEach { user ->
                val dto = user.toDto()
                successful.add(dto)
            }
        }

        return BulkOperationResult(successful, failed)
    }

    @Transactional
    override fun bulkUpdateUsers(request: BulkUpdateUsersRequest): BulkOperationResult<UserDto> {
        val successful = mutableListOf<UserDto>()
        val failed = mutableListOf<BulkOperationError>()

        request.updates.forEachIndexed { index, updateOperation ->
            try {
                val updatedUser = updateUser(updateOperation.id, updateOperation.request)
                successful.add(updatedUser)
            } catch (e: ResourceNotFoundException) {
                failed.add(
                    BulkOperationError(
                        index = index,
                        identifier = updateOperation.id.toString(),
                        error = e.message ?: "Resource not found",
                    ),
                )
            } catch (e: DataIntegrityViolationException) {
                failed.add(
                    BulkOperationError(
                        index = index,
                        identifier = updateOperation.id.toString(),
                        error = "Data integrity violation: ${e.message}",
                    ),
                )
            } catch (e: IllegalArgumentException) {
                failed.add(
                    BulkOperationError(
                        index = index,
                        identifier = updateOperation.id.toString(),
                        error = e.message ?: "Invalid input",
                    ),
                )
            }
        }

        return BulkOperationResult(successful, failed)
    }

    @Transactional
    override fun bulkDeleteUsers(request: BulkDeleteUsersRequest): BulkOperationResult<Long> {
        val successful = mutableListOf<Long>()
        val failed = mutableListOf<BulkOperationError>()

        request.userIds.forEachIndexed { index, userId ->
            try {
                softDeleteUser(userId)
                successful.add(userId)
            } catch (e: ResourceNotFoundException) {
                failed.add(
                    BulkOperationError(
                        index = index,
                        identifier = userId.toString(),
                        error = e.message ?: "Resource not found",
                    ),
                )
            } catch (e: DataIntegrityViolationException) {
                failed.add(
                    BulkOperationError(
                        index = index,
                        identifier = userId.toString(),
                        error = "Data integrity violation: ${e.message}",
                    ),
                )
            }
        }

        return BulkOperationResult(successful, failed)
    }

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

        return result
    }

    override fun validatePasswordComplexity(password: String): PasswordValidationResult {
        val violations = mutableListOf<String>()

        // Minimum length check
        if (password.length < MIN_PASSWORD_LENGTH) {
            violations.add("Password must be at least $MIN_PASSWORD_LENGTH characters long")
        }

        // Maximum length check
        if (password.length > MAX_PASSWORD_LENGTH) {
            violations.add("Password must not exceed $MAX_PASSWORD_LENGTH characters")
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
        require(user.password != null && passwordEncoder.matches(currentPassword, user.password)) {
            "Current password is incorrect"
        }

        // Validate new password
        val validation = validatePasswordComplexity(newPassword)
        require(validation.isValid) {
            "Password validation failed: ${validation.violations.joinToString(", ")}"
        }

        // Encode and set new password
        user.password = passwordEncoder.encode(newPassword)
        val savedUser = userRepository.save(user)
        val result = savedUser.toDto()

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
        require(validation.isValid) {
            "Password validation failed: ${validation.violations.joinToString(", ")}"
        }

        // Encode and set new password
        user.password = passwordEncoder.encode(newPassword)
        val savedUser = userRepository.save(user)
        val result = savedUser.toDto()

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

    @Transactional
    override fun assignRoles(
        userId: Long,
        roleNames: Set<String>,
    ) {
        val user =
            userRepository
                .findActiveById(userId)
                .orElseThrow { createUserNotFoundException("id", userId) }

        val rolesToAssign = roleRepository.findByNameIn(roleNames)

        // Verify all roles exist
        val foundRoleNames = rolesToAssign.map { it.name }.toSet()
        val missingRoles = roleNames - foundRoleNames
        if (missingRoles.isNotEmpty()) {
            throw ResourceNotFoundException("Role", "names", missingRoles.toString())
        }

        // Add new roles to existing ones
        user.roles.addAll(rolesToAssign)

        userRepository.save(user)
    }

    @Transactional
    override fun removeRoles(
        userId: Long,
        roleNames: Set<String>,
    ) {
        val user =
            userRepository
                .findActiveById(userId)
                .orElseThrow { createUserNotFoundException("id", userId) }

        // Remove roles by name
        user.roles.removeAll { it.name in roleNames }
    }

    override fun getUserRoles(userId: Long): Set<String> {
        val user =
            userRepository
                .findActiveById(userId)
                .orElseThrow { createUserNotFoundException("id", userId) }

        return user.roles.map { it.name }.toSet()
    }

    @Transactional
    override fun setUserRoles(
        userId: Long,
        roleNames: Set<String>,
    ) {
        val user =
            userRepository
                .findActiveById(userId)
                .orElseThrow { createUserNotFoundException("id", userId) }

        val rolesToSet =
            if (roleNames.isNotEmpty()) {
                val roles = roleRepository.findByNameIn(roleNames)

                // Verify all roles exist
                val foundRoleNames = roles.map { it.name }.toSet()
                val missingRoles = roleNames - foundRoleNames
                if (missingRoles.isNotEmpty()) {
                    throw ResourceNotFoundException("Role", "names", missingRoles.toString())
                }

                roles.toSet()
            } else {
                emptySet()
            }

        // Clear existing roles and add new ones instead of replacing the collection
        // This avoids issues with Hibernate's managed collections
        user.roles.clear()
        user.roles.addAll(rolesToSet)
    }

    override fun userHasRole(
        userId: Long,
        roleName: String,
    ): Boolean {
        val user =
            userRepository
                .findActiveById(userId)
                .orElseThrow { createUserNotFoundException("id", userId) }

        return user.roles.any { it.name == roleName }
    }

    override fun getAllRoleNames(): Set<String> = roleRepository.findAllRoleNames().toSet()

    /**
     * Validates email format using a simple regex pattern.
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})\$".toRegex()
        return email.matches(emailRegex)
    }
}
