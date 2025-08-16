package com.jotoai.voenix.shop.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.jotoai.voenix.shop.auth.api.dto.LoginRequest
import com.jotoai.voenix.shop.config.TestDataConfig
import com.jotoai.voenix.shop.config.TestSecurityConfig
import com.jotoai.voenix.shop.user.internal.repository.UserRepository
import jakarta.persistence.EntityManager
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig::class, TestDataConfig::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractSecurityTest {
    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper

    @Autowired
    protected lateinit var userRepository: UserRepository

    @Autowired
    protected lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    protected lateinit var entityManager: EntityManager

    protected var adminSession: MockHttpSession? = null
    protected var userSession: MockHttpSession? = null
    protected var adminSessionCookie: Cookie? = null
    protected var userSessionCookie: Cookie? = null

    @BeforeEach
    @Transactional
    fun setUp() {
        // Note: Test users should be set up by individual test classes as needed
        // This base class provides the authentication utilities
    }

    protected fun authenticateUser(
        email: String,
        password: String,
    ): MockHttpSession {
        val loginRequest = LoginRequest(email = email, password = password)
        val result =
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)),
                ).andExpect(MockMvcResultMatchers.status().isOk)
                .andReturn()

        return result.request.session as MockHttpSession
    }

    protected fun getSessionCookie(session: MockHttpSession): Cookie = Cookie("SESSION", session.id)

    protected fun performGetAsAdmin(url: String): MvcResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get(url)
                    .session(adminSession!!)
                    .cookie(adminSessionCookie!!),
            ).andReturn()

    protected fun performGetAsUser(url: String): MvcResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .get(url)
                    .session(userSession!!)
                    .cookie(userSessionCookie!!),
            ).andReturn()

    protected fun performGetAsUnauthenticated(url: String): MvcResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get(url),
            ).andReturn()

    protected fun performPostAsAdmin(
        url: String,
        content: Any? = null,
    ): MvcResult {
        val request =
            MockMvcRequestBuilders
                .post(url)
                .session(adminSession!!)
                .cookie(adminSessionCookie!!)
                .contentType(MediaType.APPLICATION_JSON)

        if (content != null) {
            request.content(objectMapper.writeValueAsString(content))
        }

        return mockMvc.perform(request).andReturn()
    }

    protected fun performPostAsUser(
        url: String,
        content: Any? = null,
    ): MvcResult {
        val request =
            MockMvcRequestBuilders
                .post(url)
                .session(userSession!!)
                .cookie(userSessionCookie!!)
                .contentType(MediaType.APPLICATION_JSON)

        if (content != null) {
            request.content(objectMapper.writeValueAsString(content))
        }

        return mockMvc.perform(request).andReturn()
    }

    protected fun performPostAsUnauthenticated(
        url: String,
        content: Any? = null,
    ): MvcResult {
        val request =
            MockMvcRequestBuilders
                .post(url)
                .contentType(MediaType.APPLICATION_JSON)

        if (content != null) {
            request.content(objectMapper.writeValueAsString(content))
        }

        return mockMvc.perform(request).andReturn()
    }

    protected fun performPutAsAdmin(
        url: String,
        content: Any? = null,
    ): MvcResult {
        val request =
            MockMvcRequestBuilders
                .put(url)
                .session(adminSession!!)
                .cookie(adminSessionCookie!!)
                .contentType(MediaType.APPLICATION_JSON)

        if (content != null) {
            request.content(objectMapper.writeValueAsString(content))
        }

        return mockMvc.perform(request).andReturn()
    }

    protected fun performPutAsUser(
        url: String,
        content: Any? = null,
    ): MvcResult {
        val request =
            MockMvcRequestBuilders
                .put(url)
                .session(userSession!!)
                .cookie(userSessionCookie!!)
                .contentType(MediaType.APPLICATION_JSON)

        if (content != null) {
            request.content(objectMapper.writeValueAsString(content))
        }

        return mockMvc.perform(request).andReturn()
    }

    protected fun performPutAsUnauthenticated(
        url: String,
        content: Any? = null,
    ): MvcResult {
        val request =
            MockMvcRequestBuilders
                .put(url)
                .contentType(MediaType.APPLICATION_JSON)

        if (content != null) {
            request.content(objectMapper.writeValueAsString(content))
        }

        return mockMvc.perform(request).andReturn()
    }

    protected fun performDeleteAsAdmin(url: String): MvcResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .delete(url)
                    .session(adminSession!!)
                    .cookie(adminSessionCookie!!),
            ).andReturn()

    protected fun performDeleteAsUser(url: String): MvcResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders
                    .delete(url)
                    .session(userSession!!)
                    .cookie(userSessionCookie!!),
            ).andReturn()

    protected fun performDeleteAsUnauthenticated(url: String): MvcResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders.delete(url),
            ).andReturn()
}
