package com.jotoai.voenix.shop.security

import com.jotoai.voenix.shop.api.admin.articles.mugs.AdminMugController
import com.jotoai.voenix.shop.api.admin.prompts.AdminPromptController
import com.jotoai.voenix.shop.api.admin.users.AdminUserController
import com.jotoai.voenix.shop.auth.config.SecurityConfig
import com.jotoai.voenix.shop.auth.service.CustomUserDetailsService
import com.jotoai.voenix.shop.domain.articles.mugs.service.MugService
import com.jotoai.voenix.shop.domain.prompts.service.PromptService
import com.jotoai.voenix.shop.domain.users.service.UserService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(
    controllers = [
        AdminUserController::class,
        AdminPromptController::class,
        AdminMugController::class,
    ],
)
@Import(SecurityConfig::class)
@ActiveProfiles("test")
class AdminEndpointSecurityWebMvcTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var userService: UserService

    @MockitoBean
    private lateinit var promptService: PromptService

    @MockitoBean
    private lateinit var mugService: MugService

    @MockitoBean
    private lateinit var customUserDetailsService: CustomUserDetailsService

    @Test
    fun testAdminEndpointsRequireAuthentication() {
        // Test that admin endpoints return 401 when not authenticated
        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/admin/users"))
            .andExpect(status().isUnauthorized())

        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/admin/prompts"))
            .andExpect(status().isUnauthorized())

        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/admin/mugs"))
            .andExpect(status().isUnauthorized())
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = ["USER"])
    fun testAdminEndpointsRequireAdminRole() {
        // Test that admin endpoints return 403 for non-admin users
        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/admin/users"))
            .andExpect(status().isForbidden())

        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/admin/prompts"))
            .andExpect(status().isForbidden())

        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/admin/mugs"))
            .andExpect(status().isForbidden())
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = ["ADMIN"])
    fun testAdminEndpointsAccessibleWithAdminRole() {
        // Mock service responses
        `when`(userService.getAllUsers()).thenReturn(emptyList())
        `when`(promptService.getAllPrompts()).thenReturn(emptyList())
        `when`(mugService.getAllMugs()).thenReturn(emptyList())

        // Test that admin endpoints are accessible with ADMIN role (200 or other success status)
        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/admin/users"))
            .andExpect(status().isOk())

        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/admin/prompts"))
            .andExpect(status().isOk())

        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/admin/articles/mugs"))
            .andExpect(status().isOk())
    }
}
