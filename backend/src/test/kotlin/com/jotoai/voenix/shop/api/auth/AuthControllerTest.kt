package com.jotoai.voenix.shop.api.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.jotoai.voenix.shop.application.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.auth.api.AuthService
import com.jotoai.voenix.shop.auth.api.dto.LoginResponse
import com.jotoai.voenix.shop.auth.api.dto.RegisterRequest
import com.jotoai.voenix.shop.user.api.dto.UserDto
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var authService: AuthService

    @Test
    fun `register should create new user and return login response`() {
        // Given
        val registerRequest =
            RegisterRequest(
                email = "newuser@example.com",
                password = "Test123!@&",
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

        every { authService.register(registerRequest, any(), any()) } returns loginResponse

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
                password = "Test123!@&",
            )

        every {
            authService.register(registerRequest, any(), any())
        } throws ResourceAlreadyExistsException("User", "email", "existing@example.com")

        // When & Then
        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)),
            ).andExpect(status().isConflict)
            .andExpect(jsonPath("$.message").value("User already exists with email: existing@example.com"))
    }

    @Test
    fun `register should fail with invalid email format`() {
        // Given
        val registerRequest =
            RegisterRequest(
                email = "invalid-email",
                password = "Test123!@&",
            )

        // When & Then
        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)),
            ).andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.validationErrors.email").value("Invalid email format"))
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
                    "$.validationErrors.password",
                ).value(
                    "Password must contain at least one uppercase letter, " +
                        "one lowercase letter, one digit, and one special character",
                ),
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
            .andExpect(jsonPath("$.validationErrors.password").exists())
    }
}
