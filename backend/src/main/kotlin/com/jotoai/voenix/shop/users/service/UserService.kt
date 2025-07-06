package com.jotoai.voenix.shop.users.service

import com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.users.dto.CreateUserRequest
import com.jotoai.voenix.shop.users.dto.UpdateUserRequest
import com.jotoai.voenix.shop.users.dto.UserDto
import com.jotoai.voenix.shop.users.entity.User
import com.jotoai.voenix.shop.users.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {
    
    fun getAllUsers(): List<UserDto> {
        return userRepository.findAll().map { toDto(it) }
    }
    
    fun getUserById(id: Long): UserDto {
        return userRepository.findById(id)
            .map { toDto(it) }
            .orElseThrow { ResourceNotFoundException("User", "id", id) }
    }
    
    fun getUserByEmail(email: String): UserDto {
        return userRepository.findByEmail(email)
            .map { toDto(it) }
            .orElseThrow { ResourceNotFoundException("User", "email", email) }
    }
    
    @Transactional
    fun createUser(request: CreateUserRequest): UserDto {
        if (userRepository.existsByEmail(request.email)) {
            throw ResourceAlreadyExistsException("User", "email", request.email)
        }
        
        val user = User(
            email = request.email,
            firstName = request.firstName,
            lastName = request.lastName,
            phoneNumber = request.phoneNumber,
            password = request.password
        )
        
        val savedUser = userRepository.save(user)
        return toDto(savedUser)
    }
    
    @Transactional
    fun updateUser(id: Long, request: UpdateUserRequest): UserDto {
        val user = userRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("User", "id", id) }
        
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
        return toDto(updatedUser)
    }
    
    @Transactional
    fun deleteUser(id: Long) {
        if (!userRepository.existsById(id)) {
            throw ResourceNotFoundException("User", "id", id)
        }
        userRepository.deleteById(id)
    }
    
    private fun toDto(user: User): UserDto {
        return UserDto(
            id = user.id!!,
            email = user.email,
            firstName = user.firstName,
            lastName = user.lastName,
            phoneNumber = user.phoneNumber,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
}