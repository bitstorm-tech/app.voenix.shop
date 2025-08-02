package com.jotoai.voenix.shop.api.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.jotoai.voenix.shop.auth.dto.RegisterRequest
import com.jotoai.voenix.shop.auth.entity.Role
import com.jotoai.voenix.shop.auth.repository.RoleRepository
import com.jotoai.voenix.shop.domain.users.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthRegistrationIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var roleRepository: RoleRepository

    @BeforeEach
    fun setup() {
        // Clean up any existing test data
        userRepository.deleteAll()

        // Ensure roles exist (they should be created by migrations in a real app)
        if (!roleRepository.existsById(1L)) {
            roleRepository.save(Role(id = 1L, name = "ADMIN", description = "Administrator role"))
        }
        if (!roleRepository.existsById(2L)) {
            roleRepository.save(Role(id = 2L, name = "USER", description = "Regular user role"))
        }
    }

    @Test
    fun `register should create new user successfully`() {
        // Given
        val registerRequest =
            RegisterRequest(
                email = "newuser@example.com",
                password = "Test123!@#",
            )

        // When & Then
        mockMvc
            .perform(
                post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(registerRequest)),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.user.email").value("newuser@example.com"))
            .andExpect(jsonPath("$.sessionId").exists())
            .andExpect(jsonPath("$.roles[0]").value("USER"))

        // Verify user was created in database
        val savedUser = userRepository.findByEmail("newuser@example.com")
        assert(savedUser.isPresent)
        assert(savedUser.get().email == "newuser@example.com")
    }
}
