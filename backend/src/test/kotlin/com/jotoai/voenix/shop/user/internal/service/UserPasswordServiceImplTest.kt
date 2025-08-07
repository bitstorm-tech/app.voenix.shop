package com.jotoai.voenix.shop.user.internal.service

import com.jotoai.voenix.shop.user.events.UserUpdatedEvent
import com.jotoai.voenix.shop.user.internal.entity.User
import com.jotoai.voenix.shop.user.internal.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional

class UserPasswordServiceImplTest {
    private lateinit var userRepository: UserRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var eventPublisher: ApplicationEventPublisher
    private lateinit var passwordService: UserPasswordServiceImpl

    @BeforeEach
    fun setUp() {
        userRepository = mock()
        passwordEncoder = mock()
        eventPublisher = mock()
        passwordService = UserPasswordServiceImpl(userRepository, passwordEncoder, eventPublisher)
    }

    @Test
    fun `setEncodedPassword should update user password`() {
        // Given
        val userId = 1L
        val encodedPassword = "encoded_password_123"
        val user = createUser(userId, "test@test.com")

        whenever(userRepository.findActiveById(userId)).thenReturn(Optional.of(user))
        whenever(userRepository.save(any())).thenReturn(user)

        // When
        val result = passwordService.setEncodedPassword(userId, encodedPassword)

        // Then
        assertEquals(encodedPassword, user.password)
        verify(userRepository).save(user)
        verify(eventPublisher).publishEvent(any<UserUpdatedEvent>())
        assertEquals(userId, result.id)
    }

    @Test
    fun `validatePasswordComplexity should return valid for strong password`() {
        // Given
        val strongPassword = "StrongP@ssw0rd123"

        // When
        val result = passwordService.validatePasswordComplexity(strongPassword)

        // Then
        assertTrue(result.isValid)
        assertTrue(result.violations.isEmpty())
    }

    @Test
    fun `validatePasswordComplexity should return invalid for weak password`() {
        // Given
        val weakPassword = "weak"

        // When
        val result = passwordService.validatePasswordComplexity(weakPassword)

        // Then
        assertFalse(result.isValid)
        assertTrue(result.violations.isNotEmpty())
        assertTrue(result.violations.any { it.contains("at least 8 characters") })
        assertTrue(result.violations.any { it.contains("uppercase letter") })
        assertTrue(result.violations.any { it.contains("digit") })
        assertTrue(result.violations.any { it.contains("special character") })
    }

    @Test
    fun `validatePasswordComplexity should reject common patterns`() {
        // Given
        val passwordWithCommonPattern = "MyPassword123!"

        // When
        val result = passwordService.validatePasswordComplexity(passwordWithCommonPattern)

        // Then
        assertFalse(result.isValid)
        assertTrue(result.violations.any { it.contains("common patterns") })
    }

    @Test
    fun `changePassword should validate current password and update with new one`() {
        // Given
        val userId = 1L
        val currentPassword = "currentPass123!"
        val newPassword = "NewStrongP@ss456"
        val encodedCurrentPassword = "encoded_current"
        val encodedNewPassword = "encoded_new"
        val user = createUser(userId, "test@test.com", encodedCurrentPassword)

        whenever(userRepository.findActiveById(userId)).thenReturn(Optional.of(user))
        whenever(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(true)
        whenever(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword)
        whenever(userRepository.save(any())).thenReturn(user)

        // When
        val result = passwordService.changePassword(userId, currentPassword, newPassword)

        // Then
        assertEquals(encodedNewPassword, user.password)
        verify(passwordEncoder).matches(currentPassword, encodedCurrentPassword)
        verify(passwordEncoder).encode(newPassword)
        verify(userRepository).save(user)
        verify(eventPublisher).publishEvent(any<UserUpdatedEvent>())
    }

    @Test
    fun `changePassword should throw exception for incorrect current password`() {
        // Given
        val userId = 1L
        val currentPassword = "wrongPassword"
        val newPassword = "NewStrongP@ss456"
        val encodedCurrentPassword = "encoded_current"
        val user = createUser(userId, "test@test.com", encodedCurrentPassword)

        whenever(userRepository.findActiveById(userId)).thenReturn(Optional.of(user))
        whenever(passwordEncoder.matches(currentPassword, encodedCurrentPassword)).thenReturn(false)

        // When & Then
        assertThrows<IllegalArgumentException> {
            passwordService.changePassword(userId, currentPassword, newPassword)
        }
    }

    @Test
    fun `resetPassword should update password without validation`() {
        // Given
        val userId = 1L
        val newPassword = "NewStrongP@ss456"
        val encodedNewPassword = "encoded_new"
        val user = createUser(userId, "test@test.com")

        whenever(userRepository.findActiveById(userId)).thenReturn(Optional.of(user))
        whenever(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword)
        whenever(userRepository.save(any())).thenReturn(user)

        // When
        val result = passwordService.resetPassword(userId, newPassword)

        // Then
        assertEquals(encodedNewPassword, user.password)
        verify(passwordEncoder).encode(newPassword)
        verify(userRepository).save(user)
        verify(eventPublisher).publishEvent(any<UserUpdatedEvent>())
    }

    @Test
    fun `verifyPassword should return true for matching password`() {
        // Given
        val userId = 1L
        val rawPassword = "testPassword123!"
        val encodedPassword = "encoded_password"
        val user = createUser(userId, "test@test.com", encodedPassword)

        whenever(userRepository.findActiveById(userId)).thenReturn(Optional.of(user))
        whenever(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true)

        // When
        val result = passwordService.verifyPassword(userId, rawPassword)

        // Then
        assertTrue(result)
        verify(passwordEncoder).matches(rawPassword, encodedPassword)
    }

    @Test
    fun `verifyPassword should return false for non-matching password`() {
        // Given
        val userId = 1L
        val rawPassword = "wrongPassword"
        val encodedPassword = "encoded_password"
        val user = createUser(userId, "test@test.com", encodedPassword)

        whenever(userRepository.findActiveById(userId)).thenReturn(Optional.of(user))
        whenever(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false)

        // When
        val result = passwordService.verifyPassword(userId, rawPassword)

        // Then
        assertFalse(result)
        verify(passwordEncoder).matches(rawPassword, encodedPassword)
    }

    private fun createUser(
        id: Long,
        email: String,
        password: String? = null,
    ): User =
        User(
            id = id,
            email = email,
            firstName = "Test",
            lastName = "User",
            password = password,
        )
}
