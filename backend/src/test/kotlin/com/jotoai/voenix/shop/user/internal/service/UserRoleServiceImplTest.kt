package com.jotoai.voenix.shop.user.internal.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.user.events.UserUpdatedEvent
import com.jotoai.voenix.shop.user.internal.entity.Role
import com.jotoai.voenix.shop.user.internal.entity.User
import com.jotoai.voenix.shop.user.internal.repository.RoleRepository
import com.jotoai.voenix.shop.user.internal.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import java.util.Optional

class UserRoleServiceImplTest {
    private lateinit var userRepository: UserRepository
    private lateinit var roleRepository: RoleRepository
    private lateinit var eventPublisher: ApplicationEventPublisher
    private lateinit var userRoleService: UserRoleServiceImpl

    @BeforeEach
    fun setUp() {
        userRepository = mock()
        roleRepository = mock()
        eventPublisher = mock()
        userRoleService = UserRoleServiceImpl(userRepository, roleRepository, eventPublisher)
    }

    @Test
    fun `assignRoles should add roles to user`() {
        // Given
        val userId = 1L
        val roleNames = setOf("USER", "ADMIN")
        val user = createUser(userId, "test@test.com")
        val userRole = createRole(1L, "USER")
        val adminRole = createRole(2L, "ADMIN")

        whenever(userRepository.findActiveById(userId)).thenReturn(Optional.of(user))
        whenever(roleRepository.findByNameIn(roleNames)).thenReturn(listOf(userRole, adminRole))
        whenever(userRepository.save(any())).thenReturn(user)

        // When
        userRoleService.assignRoles(userId, roleNames)

        // Then
        verify(userRepository).save(any())
        verify(eventPublisher).publishEvent(any<UserUpdatedEvent>())
        assertEquals(2, user.roles.size)
        assertTrue(user.roles.any { it.name == "USER" })
        assertTrue(user.roles.any { it.name == "ADMIN" })
    }

    @Test
    fun `assignRoles should throw exception when user not found`() {
        // Given
        val userId = 1L
        val roleNames = setOf("USER")

        whenever(userRepository.findActiveById(userId)).thenReturn(Optional.empty())

        // When & Then
        assertThrows<Exception> {
            userRoleService.assignRoles(userId, roleNames)
        }

        verify(userRepository, never()).save(any())
        verify(eventPublisher, never()).publishEvent(any())
    }

    @Test
    fun `assignRoles should throw exception when role not found`() {
        // Given
        val userId = 1L
        val roleNames = setOf("USER", "NONEXISTENT")
        val user = createUser(userId, "test@test.com")
        val userRole = createRole(1L, "USER")

        whenever(userRepository.findActiveById(userId)).thenReturn(Optional.of(user))
        whenever(roleRepository.findByNameIn(roleNames)).thenReturn(listOf(userRole)) // Missing NONEXISTENT

        // When & Then
        assertThrows<ResourceNotFoundException> {
            userRoleService.assignRoles(userId, roleNames)
        }

        verify(userRepository, never()).save(any())
        verify(eventPublisher, never()).publishEvent(any())
    }

    @Test
    fun `removeRoles should remove roles from user`() {
        // Given
        val userId = 1L
        val roleNames = setOf("ADMIN")
        val userRole = createRole(1L, "USER")
        val adminRole = createRole(2L, "ADMIN")
        val user = createUser(userId, "test@test.com", mutableSetOf(userRole, adminRole))

        whenever(userRepository.findActiveById(userId)).thenReturn(Optional.of(user))
        whenever(userRepository.save(any())).thenReturn(user)

        // When
        userRoleService.removeRoles(userId, roleNames)

        // Then
        verify(userRepository).save(any())
        verify(eventPublisher).publishEvent(any<UserUpdatedEvent>())
        assertEquals(1, user.roles.size)
        assertTrue(user.roles.any { it.name == "USER" })
        assertTrue(user.roles.none { it.name == "ADMIN" })
    }

    @Test
    fun `getUserRoles should return user role names`() {
        // Given
        val userId = 1L
        val userRole = createRole(1L, "USER")
        val adminRole = createRole(2L, "ADMIN")
        val user = createUser(userId, "test@test.com", mutableSetOf(userRole, adminRole))

        whenever(userRepository.findActiveById(userId)).thenReturn(Optional.of(user))

        // When
        val result = userRoleService.getUserRoles(userId)

        // Then
        assertEquals(setOf("USER", "ADMIN"), result)
    }

    @Test
    fun `userHasRole should return true when user has role`() {
        // Given
        val userId = 1L
        val roleName = "ADMIN"
        val userRole = createRole(1L, "USER")
        val adminRole = createRole(2L, "ADMIN")
        val user = createUser(userId, "test@test.com", mutableSetOf(userRole, adminRole))

        whenever(userRepository.findActiveById(userId)).thenReturn(Optional.of(user))

        // When
        val result = userRoleService.userHasRole(userId, roleName)

        // Then
        assertTrue(result)
    }

    @Test
    fun `getAllRoleNames should return all role names from repository`() {
        // Given
        val roleNames = listOf("USER", "ADMIN", "MODERATOR")
        whenever(roleRepository.findAllRoleNames()).thenReturn(roleNames)

        // When
        val result = userRoleService.getAllRoleNames()

        // Then
        assertEquals(setOf("USER", "ADMIN", "MODERATOR"), result)
    }

    private fun createUser(
        id: Long,
        email: String,
        roles: MutableSet<Role> = mutableSetOf(),
    ): User =
        User(
            id = id,
            email = email,
            firstName = "Test",
            lastName = "User",
            roles = roles,
        )

    private fun createRole(
        id: Long,
        name: String,
    ): Role =
        Role(
            id = id,
            name = name,
            description = "Test role $name",
        )
}
