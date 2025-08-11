package com.jotoai.voenix.shop.security

import com.jotoai.voenix.shop.article.api.ArticleFacade
import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.auth.api.AuthFacade
import com.jotoai.voenix.shop.auth.api.AuthQueryService
import com.jotoai.voenix.shop.cart.api.CartFacade
import com.jotoai.voenix.shop.cart.api.CartQueryService
import com.jotoai.voenix.shop.country.api.CountryFacade
import com.jotoai.voenix.shop.country.api.CountryQueryService
import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.image.api.ImageQueryService
import com.jotoai.voenix.shop.openai.api.OpenAIImageFacade
import com.jotoai.voenix.shop.order.api.OrderFacade
import com.jotoai.voenix.shop.order.api.OrderQueryService
import com.jotoai.voenix.shop.pdf.api.OrderPdfService
import com.jotoai.voenix.shop.pdf.api.PdfFacade
import com.jotoai.voenix.shop.pdf.api.PdfQueryService
import com.jotoai.voenix.shop.pdf.internal.service.OrderDataConverter
import com.jotoai.voenix.shop.prompt.api.PromptFacade
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.supplier.api.SupplierFacade
import com.jotoai.voenix.shop.supplier.api.SupplierQueryService
import com.jotoai.voenix.shop.user.api.UserFacade
import com.jotoai.voenix.shop.user.api.UserQueryService
import com.jotoai.voenix.shop.vat.api.VatFacade
import com.jotoai.voenix.shop.vat.api.VatQueryService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
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

    // User module mocks
    @MockitoBean
    private lateinit var userQueryService: UserQueryService

    @MockitoBean
    private lateinit var userFacade: UserFacade

    // Prompt module mocks
    @MockitoBean
    private lateinit var promptQueryService: PromptQueryService

    @MockitoBean
    private lateinit var promptFacade: PromptFacade

    // Article module mocks
    @MockitoBean
    private lateinit var articleQueryService: ArticleQueryService

    @MockitoBean
    private lateinit var articleFacade: ArticleFacade

    // Country module mocks
    @MockitoBean
    private lateinit var countryQueryService: CountryQueryService

    @MockitoBean
    private lateinit var countryFacade: CountryFacade

    // Image module mocks
    @MockitoBean
    private lateinit var imageQueryService: ImageQueryService

    @MockitoBean
    private lateinit var imageFacade: ImageFacade

    // PDF module mocks
    @MockitoBean
    private lateinit var pdfQueryService: PdfQueryService

    @MockitoBean
    private lateinit var pdfFacade: PdfFacade

    // Supplier module mocks
    @MockitoBean
    private lateinit var supplierQueryService: SupplierQueryService

    @MockitoBean
    private lateinit var supplierFacade: SupplierFacade

    // VAT module mocks
    @MockitoBean
    private lateinit var vatQueryService: VatQueryService

    @MockitoBean
    private lateinit var vatFacade: VatFacade

    // Auth module mocks
    @MockitoBean
    private lateinit var authFacade: AuthFacade

    @MockitoBean
    private lateinit var authQueryService: AuthQueryService

    // Cart module mocks
    @MockitoBean
    private lateinit var cartFacade: CartFacade

    @MockitoBean
    private lateinit var cartQueryService: CartQueryService

    // Order module mocks
    @MockitoBean
    private lateinit var orderFacade: OrderFacade

    @MockitoBean
    private lateinit var orderQueryService: OrderQueryService

    @MockitoBean
    private lateinit var orderPdfService: OrderPdfService

    @MockitoBean
    private lateinit var orderDataConverter: OrderDataConverter

    // OpenAI module mocks
    @MockitoBean
    private lateinit var openAIImageFacade: OpenAIImageFacade

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
        `when`(userQueryService.getAllUsers()).thenReturn(emptyList())
        `when`(promptQueryService.getAllPrompts()).thenReturn(emptyList())
        `when`(articleQueryService.findAll(0, 20, null, null, null, null)).thenReturn(
            com.jotoai.voenix.shop.article.api.dto.ArticlePaginatedResponse(
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