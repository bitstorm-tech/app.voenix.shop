package com.jotoai.voenix.shop.user.internal.service

import com.jotoai.voenix.shop.application.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.application.ResourceNotFoundException
import com.jotoai.voenix.shop.user.CreateUserRequest
import com.jotoai.voenix.shop.user.UpdateUserRequest
import com.jotoai.voenix.shop.user.UserDto
import com.jotoai.voenix.shop.user.UserService
import com.jotoai.voenix.shop.user.internal.entity.User
import com.jotoai.voenix.shop.user.internal.repository.RoleRepository
import com.jotoai.voenix.shop.user.internal.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of UserService (public API).
 * This service exposes only the functions needed by other modules,
 * with additional private methods for internal user management.
 */
@Service
@Transactional(readOnly = true)
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
) : UserService {
    override fun getUserById(
        id: Long,
        includeAuth: Boolean,
    ): UserDto =
        userRepository
            .findActiveById(id)
            .map { it.toDto(includeAuth) }
            .orElseThrow { ResourceNotFoundException("User", "id", id) }

    override fun getUserByEmail(
        email: String,
        includeAuth: Boolean,
    ): UserDto? =
        userRepository
            .findActiveByEmail(email)
            .map { it.toDto(includeAuth) }
            .orElse(null)

    @Transactional
    override fun createUser(request: CreateUserRequest): UserDto {
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
        return savedUser.toDto()
    }

    @Transactional
    override fun updateUser(
        id: Long,
        request: UpdateUserRequest,
    ): UserDto {
        val user =
            userRepository
                .findActiveById(id)
                .orElseThrow { ResourceNotFoundException("User", "id", id) }

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

        // Handle roles if provided
        request.roles?.let { roleNames ->
            val roles = roleRepository.findByNameIn(roleNames)
            if (roles.size != roleNames.size) {
                val foundRoleNames = roles.map { it.name }.toSet()
                val missingRoles = roleNames - foundRoleNames
                throw ResourceNotFoundException("Role(s) not found: ${missingRoles.joinToString()}", "", "")
            }
            user.roles.clear()
            user.roles.addAll(roles)
        }

        val updatedUser = userRepository.save(user)
        return updatedUser.toDto()
    }
}
