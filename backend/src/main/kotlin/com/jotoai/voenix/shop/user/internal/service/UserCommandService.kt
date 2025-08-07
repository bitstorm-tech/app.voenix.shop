package com.jotoai.voenix.shop.user.internal.service

import com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.user.api.UserFacade
import com.jotoai.voenix.shop.user.api.dto.BulkCreateUsersRequest
import com.jotoai.voenix.shop.user.api.dto.BulkDeleteUsersRequest
import com.jotoai.voenix.shop.user.api.dto.BulkOperationError
import com.jotoai.voenix.shop.user.api.dto.BulkOperationResult
import com.jotoai.voenix.shop.user.api.dto.BulkUpdateUsersRequest
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
import java.time.OffsetDateTime

/**
 * Implementation of UserFacade for command operations (create, update, delete).
 * This service handles all write operations for users.
 */
@Service
@Transactional(readOnly = true)
class UserCommandService(
    private val userRepository: UserRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : UserFacade {
    @Transactional
    override fun createUser(request: CreateUserRequest): UserDto {
        // Validate email format
        if (!isValidEmail(request.email)) {
            throw IllegalArgumentException("Invalid email format: ${request.email}")
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
                password = request.password,
            )

        val savedUser = userRepository.save(user)
        val result = savedUser.toDto()

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
        request.password?.let { user.password = it }
        request.oneTimePassword?.let { user.oneTimePassword = it }

        val updatedUser = userRepository.save(user)
        val result = updatedUser.toDto()

        eventPublisher.publishEvent(UserUpdatedEvent(result))
        return result
    }

    @Transactional
    override fun softDeleteUser(id: Long) {
        val user =
            userRepository
                .findActiveById(id)
                .orElseThrow { createUserNotFoundException("id", id) }

        user.markAsDeleted()
        val deletedUser = userRepository.save(user)

        eventPublisher.publishEvent(
            UserDeletedEvent(
                userId = id,
                userEmail = deletedUser.email,
                deletedAt = deletedUser.deletedAt ?: OffsetDateTime.now(),
                isHardDelete = false,
            ),
        )
    }

    @Transactional
    override fun deleteUser(id: Long) {
        val user =
            userRepository
                .findById(id) // Use findById to include soft-deleted users for hard delete
                .orElseThrow { createUserNotFoundException("id", id) }

        val userEmail = user.email
        userRepository.deleteById(id)

        eventPublisher.publishEvent(
            UserDeletedEvent(
                userId = id,
                userEmail = userEmail,
                deletedAt = OffsetDateTime.now(),
                isHardDelete = true,
            ),
        )
    }

    @Transactional
    override fun restoreUser(id: Long): UserDto {
        val user =
            userRepository
                .findById(id) // Find even soft-deleted users
                .orElseThrow { createUserNotFoundException("id", id) }

        if (user.isActive()) {
            throw IllegalArgumentException("User with ID $id is not deleted and cannot be restored")
        }

        user.restore()
        val restoredUser = userRepository.save(user)
        val result = restoredUser.toDto()

        eventPublisher.publishEvent(UserUpdatedEvent(result))
        return result
    }

    // Bulk Operations

    @Transactional
    override fun bulkCreateUsers(request: BulkCreateUsersRequest): BulkOperationResult<UserDto> {
        val successful = mutableListOf<UserDto>()
        val failed = mutableListOf<BulkOperationError>()
        val usersToCreate = mutableListOf<User>()

        // Validate all users first
        request.users.forEachIndexed { index, userRequest ->
            try {
                // Validate email format
                if (!isValidEmail(userRequest.email)) {
                    throw IllegalArgumentException("Invalid email format: ${userRequest.email}")
                }
                
                if (userRepository.existsActiveByEmail(userRequest.email)) {
                    throw ResourceAlreadyExistsException("User", "email", userRequest.email)
                }
                
                val user = User(
                    email = userRequest.email,
                    firstName = userRequest.firstName,
                    lastName = userRequest.lastName,
                    phoneNumber = userRequest.phoneNumber,
                    password = userRequest.password,
                )
                usersToCreate.add(user)
            } catch (e: Exception) {
                failed.add(
                    BulkOperationError(
                        index = index,
                        identifier = userRequest.email,
                        error = e.message ?: "Unknown error",
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
                eventPublisher.publishEvent(UserCreatedEvent(dto))
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
            } catch (e: Exception) {
                failed.add(
                    BulkOperationError(
                        index = index,
                        identifier = updateOperation.id.toString(),
                        error = e.message ?: "Unknown error",
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
            } catch (e: Exception) {
                failed.add(
                    BulkOperationError(
                        index = index,
                        identifier = userId.toString(),
                        error = e.message ?: "Unknown error",
                    ),
                )
            }
        }

        return BulkOperationResult(successful, failed)
    }
    
    /**
     * Validates email format using a simple regex pattern.
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})\$".toRegex()
        return email.matches(emailRegex)
    }
}
