package com.jotoai.voenix.shop.user.internal.service

import com.jotoai.voenix.shop.user.api.UserQueryService
import com.jotoai.voenix.shop.user.api.dto.UserDto
import com.jotoai.voenix.shop.user.api.dto.UserSearchCriteria
import com.jotoai.voenix.shop.user.api.exceptions.createUserNotFoundException
import com.jotoai.voenix.shop.user.internal.repository.UserRepository
import com.jotoai.voenix.shop.user.internal.repository.UserSpecifications
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of UserQueryService for read-only user operations.
 * This service handles all query operations for users.
 */
@Service
@Transactional(readOnly = true)
class UserQueryServiceImpl(
    private val userRepository: UserRepository,
) : UserQueryService {
    override fun getAllUsers(): List<UserDto> = userRepository.findAllActive().map { it.toDto() }

    override fun getAllUsers(pageable: Pageable): Page<UserDto> = 
        userRepository.findAllActive(pageable).map { it.toDto() }

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

    override fun getUsersByIds(ids: List<Long>): List<UserDto> = 
        userRepository.findActiveByIds(ids).map { it.toDto() }
}
