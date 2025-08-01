package com.jotoai.voenix.shop.api.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.jotoai.voenix.shop.auth.dto.LoginResponse
import com.jotoai.voenix.shop.auth.dto.RegisterRequest
import com.jotoai.voenix.shop.auth.repository.RoleRepository
import com.jotoai.voenix.shop.auth.service.AuthService
import com.jotoai.voenix.shop.auth.service.UserRegistrationService
import com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.config.TestSecurityConfig
import com.jotoai.voenix.shop.domain.users.dto.UserDto
import com.jotoai.voenix.shop.domain.users.repository.UserRepository
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime

@WebMvcTest(AuthController::class)
@ActiveProfiles("test")
@Import(TestSecurityConfig::class)
class AuthControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var authService: AuthService

    @MockBean
    private lateinit var userRegistrationService: UserRegistrationService

    @MockBean
    private lateinit var userRepository: UserRepository

    @MockBean
    private lateinit var roleRepository: RoleRepository

    @MockBean
    private lateinit var passwordEncoder: PasswordEncoder

    @MockBean
    private lateinit var authenticationManager: AuthenticationManager

    @MockBean
    private lateinit var securityContextRepository: SecurityContextRepository

    @Test
    fun `register should create new user and return login response`() {
        // Given
        val registerRequest =
            RegisterRequest(
                email = "newuser@example.com",
                password = "Test123!@#",
            )

        val userDto =
            UserDto(
                id = 1L,
                email = "newuser@example.com",
                firstName = null,
                lastName = null,
                phoneNumber = null,
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )

        val loginResponse =
            LoginResponse(
                user = userDto,
                sessionId = "test-session-id",
                roles = listOf("USER"),
            )

        whenever(authService.register(eq(registerRequest), any(), any())).thenReturn(loginResponse)

        // When & Then
        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.user.email").value("newuser@example.com"))
            .andExpect(jsonPath("$.sessionId").value("test-session-id"))
            .andExpect(jsonPath("$.roles[0]").value("USER"))
    }

    @Test
    fun `register should fail with duplicate email`() {
        // Given
        val registerRequest =
            RegisterRequest(
                email = "existing@example.com",
                password = "Test123!@#",
            )

        whenever(authService.register(eq(registerRequest), any(), any()))
            .thenThrow(ResourceAlreadyExistsException("User", "email", "existing@example.com"))

        // When & Then
        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)),
            ).andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").value("User with email existing@example.com already exists"))
    }

    @Test
    fun `register should fail with invalid email format`() {
        // Given
        val registerRequest =
            RegisterRequest(
                email = "invalid-email",
                password = "Test123!@#",
            )

        // When & Then
        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors.email").value("Invalid email format"))
    }

    @Test
    fun `register should fail with weak password`() {
        // Given - password without special character
        val registerRequest =
            RegisterRequest(
                email = "test@example.com",
                password = "Test1234",
            )

        // When & Then
        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)),
            ).andExpect(status().isBadRequest)
            .andExpect(
                jsonPath(
                    "$.errors.password",
                ).value("Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"),
            )
    }

    @Test
    fun `register should fail with short password`() {
        // Given
        val registerRequest =
            RegisterRequest(
                email = "test@example.com",
                password = "Test1!",
            )

        // When & Then
        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.errors.password").exists())
    }
}
