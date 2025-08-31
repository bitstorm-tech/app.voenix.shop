package com.jotoai.voenix.shop.auth.internal.service

import com.jotoai.voenix.shop.application.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.application.ResourceNotFoundException
import com.jotoai.voenix.shop.auth.api.AuthService
import com.jotoai.voenix.shop.auth.api.dto.LoginRequest
import com.jotoai.voenix.shop.auth.api.dto.RegisterGuestRequest
import com.jotoai.voenix.shop.auth.api.dto.RegisterRequest
import com.jotoai.voenix.shop.auth.api.dto.UserCreationRequest
import com.jotoai.voenix.shop.auth.internal.security.CustomUserDetails
import com.jotoai.voenix.shop.user.api.UserService
import com.jotoai.voenix.shop.user.api.dto.CreateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UpdateUserRequest
import com.jotoai.voenix.shop.user.api.dto.UserAuthenticationDto
import com.jotoai.voenix.shop.user.api.dto.UserDto
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.context.SecurityContextRepository
import java.time.OffsetDateTime

class AuthServiceImplTest {
    private lateinit var authService: AuthService
    private lateinit var authenticationManager: AuthenticationManager
    private lateinit var userService: UserService
    private lateinit var securityContextRepository: SecurityContextRepository
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var httpRequest: HttpServletRequest
    private lateinit var httpResponse: HttpServletResponse
    private lateinit var httpSession: HttpSession
    private lateinit var securityContext: SecurityContext
    private lateinit var authentication: Authentication

    @BeforeEach
    fun setUp() {
        authenticationManager = mockk()
        userService = mockk()
        securityContextRepository = mockk()
        passwordEncoder = mockk()
        httpRequest = mockk()
        httpResponse = mockk()
        httpSession = mockk()
        securityContext = mockk()
        authentication = mockk()

        authService =
            AuthServiceImpl(
                authenticationManager = authenticationManager,
                userService = userService,
                securityContextRepository = securityContextRepository,
                passwordEncoder = passwordEncoder,
            )

        SecurityContextHolder.clearContext()
    }

    @Test
    fun `login should authenticate user and return session info`() {
        val loginRequest = LoginRequest(email = "test@example.com", password = "password123")
        val userDto = createTestUser()
        val userRoles = setOf("USER")
        val principal =
            CustomUserDetails(
                id = 1L,
                email = "test@example.com",
                passwordHash = "encoded",
                userRoles = userRoles,
            )

        every { httpRequest.getSession(true) } returns httpSession
        every { httpRequest.getSession(false) } returns httpSession
        every { httpSession.id } returns "session-123"
        every { httpRequest.changeSessionId() } returns "session-123"
        every { securityContextRepository.saveContext(any(), any(), any()) } just runs
        every { authenticationManager.authenticate(any()) } returns authentication
        every { authentication.principal } returns principal
        every { userService.getUserById(1L) } returns userDto
        every { userService.getUserRoles(1L) } returns userRoles

        val result = authService.login(loginRequest, httpRequest, httpResponse)

        assertNotNull(result)
        assertEquals("session-123", result.sessionId)
        assertEquals(userDto, result.user)
        assertEquals(userRoles.toList(), result.roles)

        verify { securityContextRepository.saveContext(any(), httpRequest, httpResponse) }
        verify { authenticationManager.authenticate(any()) }
    }

    @Test
    fun `login should throw InvalidCredentialsException for invalid credentials`() {
        val loginRequest = LoginRequest(email = "test@example.com", password = "wrongpassword")

        every { authenticationManager.authenticate(any()) } throws BadCredentialsException("Invalid credentials")

        assertThrows<com.jotoai.voenix.shop.auth.api.exceptions.InvalidCredentialsException> {
            authService.login(loginRequest, httpRequest, httpResponse)
        }
    }

    @Test
    fun `getCurrentSession should return authenticated session for valid principal`() {
        val userDto = createTestUser()
        val userRoles = setOf("USER")
        val principal =
            CustomUserDetails(
                id = 1L,
                email = "test@example.com",
                passwordHash = "encoded",
                userRoles = userRoles,
            )

        SecurityContextHolder.getContext().authentication = authentication
        every { authentication.isAuthenticated } returns true
        every { authentication.principal } returns principal
        every { userService.getUserById(1L) } returns userDto
        every { userService.getUserRoles(1L) } returns userRoles

        val result = authService.getCurrentSession()

        assertTrue(result.authenticated)
        assertEquals(userDto, result.user)
        assertEquals(userRoles.toList(), result.roles)
    }

    @Test
    fun `getCurrentSession should return unauthenticated session when not authenticated`() {
        SecurityContextHolder.getContext().authentication = authentication
        every { authentication.isAuthenticated } returns false

        val result = authService.getCurrentSession()

        assertFalse(result.authenticated)
        assertEquals(null, result.user)
        assertEquals(emptyList<String>(), result.roles)
    }

    @Test
    fun `getCurrentSession should return unauthenticated session when user not found`() {
        val principal =
            CustomUserDetails(
                id = 999L,
                email = "nonexistent@example.com",
                passwordHash = "encoded",
                userRoles = setOf(),
            )

        SecurityContextHolder.getContext().authentication = authentication
        every { authentication.isAuthenticated } returns true
        every { authentication.principal } returns principal
        every { userService.getUserById(999L) } throws ResourceNotFoundException("User", "id", "999")

        val result = authService.getCurrentSession()

        assertFalse(result.authenticated)
    }

    @Test
    fun `getCurrentSession should return unauthenticated session when no authentication`() {
        val result = authService.getCurrentSession()

        assertFalse(result.authenticated)
        assertEquals(null, result.user)
        assertEquals(emptyList<String>(), result.roles)
    }

    @Test
    fun `register should create new user and authenticate`() {
        val registerRequest =
            RegisterRequest(
                email = "newuser@example.com",
                password = "password123",
            )
        val userDto = createTestUser(email = "newuser@example.com")
        val userRoles = setOf("USER")
        val principal =
            CustomUserDetails(
                id = 1L,
                email = "newuser@example.com",
                passwordHash = "encoded",
                userRoles = userRoles,
            )

        every { passwordEncoder.encode("password123") } returns "encoded-password"
        every { userService.existsByEmail("newuser@example.com") } returns false
        every { userService.setUserRoles(1L, setOf("USER")) } just runs
        every { httpRequest.getSession(true) } returns httpSession
        every { httpRequest.getSession(false) } returns httpSession
        every { httpSession.id } returns "session-123"
        every { httpRequest.changeSessionId() } returns "session-123"
        every { securityContextRepository.saveContext(any(), any(), any()) } just runs
        every { authenticationManager.authenticate(any()) } returns authentication
        every { authentication.principal } returns principal
        every { userService.createUser(any()) } returns userDto
        every { userService.getUserById(1L) } returns userDto
        every { userService.getUserRoles(1L) } returns userRoles

        val result = authService.register(registerRequest, httpRequest, httpResponse)

        assertNotNull(result)
        assertEquals("session-123", result.sessionId)
        assertEquals(userDto, result.user)

        val captor = slot<CreateUserRequest>()
        verify { userService.createUser(capture(captor)) }
        val createRequest = captor.captured
        assertEquals("newuser@example.com", createRequest.email)
        assertEquals("encoded-password", createRequest.password)
        // Default values for first name, last name, and phone number are null in the implementation
    }

    @Test
    fun `registerGuest should create new guest user without password`() {
        val registerRequest =
            RegisterGuestRequest(
                email = "guest@example.com",
                firstName = "Guest",
                lastName = "User",
                phoneNumber = "+1234567890",
            )
        val userDto = createTestUser(email = "guest@example.com")
        val userRoles = setOf("USER")
        val principal =
            CustomUserDetails(
                id = 1L,
                email = "guest@example.com",
                passwordHash = null,
                userRoles = userRoles,
            )

        every { userService.existsByEmail("guest@example.com") } returns false
        every { userService.loadUserByEmail("guest@example.com") } returns null
        every { userService.setUserRoles(1L, setOf("USER")) } just runs
        every { httpRequest.getSession(true) } returns httpSession
        every { httpRequest.getSession(false) } returns httpSession
        every { httpSession.id } returns "session-123"
        every { httpRequest.changeSessionId() } returns "session-123"
        every { securityContextRepository.saveContext(any(), any(), any()) } just runs
        every { authenticationManager.authenticate(any()) } returns authentication
        every { authentication.principal } returns principal
        every { userService.createUser(any()) } returns userDto
        every { userService.getUserById(1L) } returns userDto
        every { userService.getUserRoles(1L) } returns userRoles

        val result = authService.registerGuest(registerRequest, httpRequest, httpResponse)

        assertNotNull(result)
        assertEquals("session-123", result.sessionId)
        assertEquals(userDto, result.user)

        val captor = slot<CreateUserRequest>()
        verify { userService.createUser(capture(captor)) }
        val createRequest = captor.captured
        assertEquals("guest@example.com", createRequest.email)
        assertEquals(null, createRequest.password)
        assertEquals("Guest", createRequest.firstName)
        assertEquals("User", createRequest.lastName)
    }

    @Test
    fun `registerGuest should update existing guest user`() {
        val registerRequest =
            RegisterGuestRequest(
                email = "guest@example.com",
                firstName = "UpdatedGuest",
                lastName = "UpdatedUser",
                phoneNumber = "+9876543210",
            )
        val existingUser = createTestUser(id = 5L, email = "guest@example.com")
        val updatedUser = existingUser.copy(firstName = "UpdatedGuest", lastName = "UpdatedUser")
        val userRoles = setOf("USER")
        val principal =
            CustomUserDetails(
                id = 5L,
                email = "guest@example.com",
                passwordHash = null,
                userRoles = userRoles,
            )

        every { userService.existsByEmail("guest@example.com") } returns true
        every { userService.loadUserByEmail("guest@example.com") } returns
            UserAuthenticationDto(
                id = existingUser.id,
                email = existingUser.email,
                passwordHash = null,
                roles = setOf("USER"),
                isActive = true,
            )
        every { httpRequest.getSession(true) } returns httpSession
        every { httpRequest.getSession(false) } returns httpSession
        every { httpSession.id } returns "session-123"
        every { httpRequest.changeSessionId() } returns "session-123"
        every { securityContextRepository.saveContext(any(), any(), any()) } just runs
        every { authenticationManager.authenticate(any()) } returns authentication
        every { authentication.principal } returns principal
        every { userService.updateUser(any(), any()) } returns updatedUser
        every { userService.getUserById(5L) } returns updatedUser
        every { userService.getUserRoles(5L) } returns userRoles

        val result = authService.registerGuest(registerRequest, httpRequest, httpResponse)

        assertNotNull(result)
        assertEquals("session-123", result.sessionId)
        assertEquals(updatedUser, result.user)

        val idCaptor = slot<Long>()
        val requestCaptor = slot<UpdateUserRequest>()
        verify { userService.updateUser(capture(idCaptor), capture(requestCaptor)) }
        assertEquals(5L, idCaptor.captured)
        val updateRequest = requestCaptor.captured
        assertEquals("UpdatedGuest", updateRequest.firstName)
        assertEquals("UpdatedUser", updateRequest.lastName)
        assertEquals("+9876543210", updateRequest.phoneNumber)
    }

    @Test
    fun `registerGuest should throw exception when email exists with password`() {
        val registerRequest =
            RegisterGuestRequest(
                email = "existing@example.com",
                firstName = "Existing",
                lastName = "User",
            )
        // Simulate user that has a password (not a guest user)
        val existingUser = createTestUser(email = "existing@example.com")

        every { userService.existsByEmail("existing@example.com") } returns true
        every { userService.loadUserByEmail("existing@example.com") } returns
            UserAuthenticationDto(
                id = existingUser.id,
                email = existingUser.email,
                passwordHash = "hashedPassword",
                roles = setOf("USER"),
            )

        assertThrows<ResourceAlreadyExistsException> {
            authService.registerGuest(registerRequest, httpRequest, httpResponse)
        }
    }

    @Test
    fun `createUser should create user with all fields`() {
        val userDto = createTestUser()

        every { userService.createUser(any()) } returns userDto
        every { userService.setUserRoles(1L, setOf("USER", "ADMIN")) } just runs
        every { passwordEncoder.encode("password123") } returns "encoded-password"

        val result =
            authService.createUser(
                UserCreationRequest(
                    email = "test@example.com",
                    password = "password123",
                    firstName = "John",
                    lastName = "Doe",
                    phoneNumber = "+1234567890",
                    roleNames = setOf("USER", "ADMIN"),
                ),
            )

        assertEquals(userDto, result)

        val captor = slot<CreateUserRequest>()
        verify { userService.createUser(capture(captor)) }
        val request = captor.captured
        assertEquals("test@example.com", request.email)
        assertEquals("encoded-password", request.password)
        assertEquals("John", request.firstName)
        assertEquals("Doe", request.lastName)
        assertEquals("+1234567890", request.phoneNumber)
        verify { userService.setUserRoles(1L, setOf("USER", "ADMIN")) }
    }

    @Test
    fun `createUser should create user without password for guest`() {
        val userDto = createTestUser()

        every { userService.createUser(any()) } returns userDto
        every { userService.setUserRoles(1L, setOf("USER")) } just runs

        val result =
            authService.createUser(
                UserCreationRequest(
                    email = "guest@example.com",
                    password = null,
                    firstName = "Guest",
                    lastName = null,
                    phoneNumber = null,
                    roleNames = setOf("USER"),
                ),
            )

        assertEquals(userDto, result)

        val captor = slot<CreateUserRequest>()
        verify { userService.createUser(capture(captor)) }
        val request = captor.captured
        assertEquals("guest@example.com", request.email)
        assertEquals(null, request.password)
        assertEquals("Guest", request.firstName)
        assertEquals(null, request.lastName)
        assertEquals(null, request.phoneNumber)
    }

    @Test
    fun `updateUser should update user fields`() {
        val userDto = createTestUser()

        every { userService.updateUser(any(), any()) } returns userDto

        val result =
            authService.updateUser(
                userId = 1L,
                firstName = "UpdatedJohn",
                lastName = "UpdatedDoe",
                phoneNumber = "+9876543210",
            )

        assertEquals(userDto, result)

        val idCaptor = slot<Long>()
        val requestCaptor = slot<UpdateUserRequest>()
        verify { userService.updateUser(capture(idCaptor), capture(requestCaptor)) }
        assertEquals(1L, idCaptor.captured)
        val request = requestCaptor.captured
        assertEquals("UpdatedJohn", request.firstName)
        assertEquals("UpdatedDoe", request.lastName)
        assertEquals("+9876543210", request.phoneNumber)
    }

    private fun createTestUser(
        id: Long = 1L,
        email: String = "test@example.com",
    ): UserDto =
        UserDto(
            id = id,
            email = email,
            firstName = "John",
            lastName = "Doe",
            phoneNumber = "+1234567890",
            createdAt = OffsetDateTime.parse("2024-01-01T00:00:00Z"),
            updatedAt = OffsetDateTime.parse("2024-01-01T00:00:00Z"),
        )
}
