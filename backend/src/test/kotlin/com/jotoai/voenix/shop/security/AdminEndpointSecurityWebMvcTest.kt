package com.jotoai.voenix.shop.security

import com.jotoai.voenix.shop.api.admin.articles.ArticleController
import com.jotoai.voenix.shop.api.admin.prompts.AdminPromptController
import com.jotoai.voenix.shop.api.admin.users.AdminUserController
import com.jotoai.voenix.shop.auth.config.SecurityConfig
import com.jotoai.voenix.shop.auth.service.CustomUserDetailsService
import com.jotoai.voenix.shop.domain.articles.service.ArticleService
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
        ArticleController::class,
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
    private lateinit var articleService: ArticleService

    @MockitoBean
    private lateinit var articleCategoryRepository: com.jotoai.voenix.shop.domain.articles.categories.repository.ArticleCategoryRepository

    @MockitoBean
    private lateinit var articleSubCategoryRepository:
        com.jotoai.voenix.shop.domain.articles.categories.repository.ArticleSubCategoryRepository

    @MockitoBean
    private lateinit var mugDetailsService: com.jotoai.voenix.shop.domain.articles.service.MugDetailsService

    @MockitoBean
    private lateinit var shirtDetailsService: com.jotoai.voenix.shop.domain.articles.service.ShirtDetailsService

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
            .perform(MockMvcRequestBuilders.get("/api/admin/articles"))
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
            .perform(MockMvcRequestBuilders.get("/api/admin/articles"))
            .andExpect(status().isForbidden())
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = ["ADMIN"])
    fun testAdminEndpointsAccessibleWithAdminRole() {
        // Mock service responses
        `when`(userService.getAllUsers()).thenReturn(emptyList())
        `when`(promptService.getAllPrompts()).thenReturn(emptyList())
        `when`(articleService.findAll(0, 20, null, null, null, null, null)).thenReturn(
            com.jotoai.voenix.shop.common.dto.PaginatedResponse(
                content = emptyList(),
                currentPage = 0,
                totalPages = 0,
                totalElements = 0,
                size = 20,
            ),
        )

        // Test that admin endpoints are accessible with ADMIN role (200 or other success status)
        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/admin/users"))
            .andExpect(status().isOk())

        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/admin/prompts"))
            .andExpect(status().isOk())

        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/admin/articles"))
            .andExpect(status().isOk())
    }
}
