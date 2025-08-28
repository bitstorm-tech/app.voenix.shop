package com.jotoai.voenix.shop.security

import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.article.api.ArticleFacade
import com.jotoai.voenix.shop.article.api.dto.FindArticlesQuery
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AdminEndpointSecurityWebMvcTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var promptQueryService: PromptQueryService

    @MockkBean
    private lateinit var articleQueryService: ArticleQueryService

    @MockkBean
    private lateinit var articleFacade: ArticleFacade

    @Test
    fun testAdminEndpointsRequireAuthentication() {
        // Test that admin endpoints return 401 when not authenticated
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
        every { promptQueryService.getAllPrompts() } returns emptyList()
        every { articleQueryService.findAll(FindArticlesQuery(page = 0, size = 20)) } returns
            com.jotoai.voenix.shop.article.api.dto.ArticlePaginatedResponse(
                content = emptyList(),
                currentPage = 0,
                totalPages = 0,
                totalElements = 0,
                size = 20,
            )

        // Test that admin endpoints are accessible with ADMIN role (200 or other success status)
        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/admin/prompts"))
            .andExpect(status().isOk())

        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/admin/articles"))
            .andExpect(status().isOk())
    }
}
