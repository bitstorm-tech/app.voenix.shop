package com.jotoai.voenix.shop.user.internal.service

import com.jotoai.voenix.shop.auth.dto.CustomUserDetails
import com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.user.api.UserAuthenticationService
import com.jotoai.voenix.shop.user.api.UserFacade
import com.jotoai.voenix.shop.user.api.UserQueryService
import com.jotoai.voenix.shop.user.api.dto.CreateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UpdateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UserDto
import com.jotoai.voenix.shop.user.api.exceptions.createUserNotFoundException
import com.jotoai.voenix.shop.user.events.UserCreatedEvent
import com.jotoai.voenix.shop.user.events.UserDeletedEvent
import com.jotoai.voenix.shop.user.events.UserUpdatedEvent
import com.jotoai.voenix.shop.user.internal.entity.User
import com.jotoai.voenix.shop.user.internal.repository.UserRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : UserFacade,
    UserQueryService,
    UserAuthenticationService {
    // UserQueryService methods
    override fun getAllUsers(): List<UserDto> = userRepository.findAll().map { it.toDto() }

    override fun getUserById(id: Long): UserDto =
        userRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { createUserNotFoundException("id", id) }

    override fun getUserByEmail(email: String): UserDto =
        userRepository
            .findByEmail(email)
            .map { it.toDto() }
            .orElseThrow { createUserNotFoundException("email", email) }

    override fun existsByEmail(email: String): Boolean = userRepository.existsByEmail(email)

    // UserFacade methods
    @Transactional
    override fun createUser(request: CreateUserRequest): UserDto {
        if (userRepository.existsByEmail(request.email)) {
            throw ResourceAlreadyExistsException("User", "email", request.email)
        }

        val user =
            User(
                email = request.email,
                firstName = request.firstName,
                lastName = request.lastName,
                phoneNumber = request.phoneNumber,
                password = request.password,
            )

        val savedUser = userRepository.save(user)
        val result = savedUser.toDto()

        // Publish event
        eventPublisher.publishEvent(UserCreatedEvent(result))

        return result
    }

    @Transactional
    override fun updateUser(
        id: Long,
        request: UpdateUserRequest,
    ): UserDto {
        val user =
            userRepository
                .findById(id)
                .orElseThrow { createUserNotFoundException("id", id) }

        val oldDto = user.toDto()

        request.email?.let { email ->
            if (email != user.email && userRepository.existsByEmail(email)) {
                throw ResourceAlreadyExistsException("User", "email", email)
            }
            user.email = email
        }

        request.firstName?.let { user.firstName = it }
        request.lastName?.let { user.lastName = it }
        request.phoneNumber?.let { user.phoneNumber = it }
        request.password?.let { user.password = it }
        request.oneTimePassword?.let { user.oneTimePassword = it }

        val updatedUser = userRepository.save(user)
        val result = updatedUser.toDto()

        // Publish event
        eventPublisher.publishEvent(UserUpdatedEvent(result))

        return result
    }

    @Transactional
    override fun deleteUser(id: Long) {
        val user =
            userRepository
                .findById(id)
                .orElseThrow { createUserNotFoundException("id", id) }

        val userEmail = user.email
        userRepository.deleteById(id)

        // Publish event
        eventPublisher.publishEvent(UserDeletedEvent(id, userEmail))
    }

    // UserAuthenticationService methods
    override fun loadUserByEmail(email: String): CustomUserDetails? {
        val user = userRepository.findByEmail(email).orElse(null) ?: return null

        return CustomUserDetails(
            id = user.id!!,
            email = user.email,
            passwordHash = user.password,
            userRoles = user.roles.map { it.name }.toSet(),
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
                .findById(id)
                .orElseThrow { createUserNotFoundException("id", id) }

        password?.let { user.password = it }
        oneTimePassword?.let { user.oneTimePassword = it }

        val updatedUser = userRepository.save(user)
        val result = updatedUser.toDto()

        // Publish event for authentication-related updates
        eventPublisher.publishEvent(UserUpdatedEvent(result))

        return result
    }
}
