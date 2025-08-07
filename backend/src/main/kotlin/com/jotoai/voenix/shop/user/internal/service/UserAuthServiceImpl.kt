package com.jotoai.voenix.shop.user.internal.service

import com.jotoai.voenix.shop.user.api.UserAuthenticationService
import com.jotoai.voenix.shop.user.api.dto.UserAuthenticationDto
import com.jotoai.voenix.shop.user.api.dto.UserDto
import com.jotoai.voenix.shop.user.api.exceptions.createUserNotFoundException
import com.jotoai.voenix.shop.user.events.UserUpdatedEvent
import com.jotoai.voenix.shop.user.internal.repository.UserRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

/**
 * Implementation of UserAuthenticationService for authentication-related user operations.
 * This service handles authentication-specific operations to avoid circular dependencies.
 */
@Service
@Transactional(readOnly = true)
class UserAuthServiceImpl(
    private val userRepository: UserRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : UserAuthenticationService {
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
            user.password = it
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

            // Publish event for authentication-related updates
            eventPublisher.publishEvent(UserUpdatedEvent(result))

            return result
        }

        return user.toDto()
    }
}
