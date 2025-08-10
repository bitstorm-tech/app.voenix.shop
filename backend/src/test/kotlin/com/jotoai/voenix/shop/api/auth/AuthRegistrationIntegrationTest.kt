package com.jotoai.voenix.shop.api.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.jotoai.voenix.shop.auth.api.dto.RegisterRequest
import com.jotoai.voenix.shop.user.internal.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql(
    statements = [
        "CREATE TABLE IF NOT EXISTS SPRING_SESSION (PRIMARY_ID CHAR(36) NOT NULL, SESSION_ID CHAR(36) NOT NULL, CREATION_TIME BIGINT NOT NULL, LAST_ACCESS_TIME BIGINT NOT NULL, MAX_INACTIVE_INTERVAL INT NOT NULL, EXPIRY_TIME BIGINT NOT NULL, PRINCIPAL_NAME VARCHAR(100), CONSTRAINT SPRING_SESSION_PK PRIMARY KEY (PRIMARY_ID))",
        "CREATE TABLE IF NOT EXISTS SPRING_SESSION_ATTRIBUTES (SESSION_PRIMARY_ID CHAR(36) NOT NULL, ATTRIBUTE_NAME VARCHAR(200) NOT NULL, ATTRIBUTE_BYTES LONGVARBINARY NOT NULL, CONSTRAINT SPRING_SESSION_ATTRIBUTES_PK PRIMARY KEY (SESSION_PRIMARY_ID, ATTRIBUTE_NAME))",
        "CREATE UNIQUE INDEX IF NOT EXISTS SPRING_SESSION_IX1 ON SPRING_SESSION (SESSION_ID)",
        "CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX2 ON SPRING_SESSION (EXPIRY_TIME)",
        "CREATE INDEX IF NOT EXISTS SPRING_SESSION_IX3 ON SPRING_SESSION (PRINCIPAL_NAME)",
        "INSERT INTO roles (name, description, created_at, updated_at) VALUES ('ADMIN', 'Administrator role', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
        "INSERT INTO roles (name, description, created_at, updated_at) VALUES ('USER', 'Regular user role', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
    ],
)
class AuthRegistrationIntegrationTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setup() {
        // Clean up any existing test data
        userRepository.deleteAll()
        // Roles are created by @Sql annotation
    }

    @Test
    fun `register should create new user successfully`() {
        // Given
        val registerRequest =
            RegisterRequest(
                email = "newuser@example.com",
                password = "TestPass123!",
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
