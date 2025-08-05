package com.jotoai.voenix.shop.api.public.mugs

import com.jotoai.voenix.shop.auth.config.SecurityConfig
import com.jotoai.voenix.shop.domain.articles.dto.PublicMugDto
import com.jotoai.voenix.shop.domain.articles.dto.PublicMugVariantDto
import com.jotoai.voenix.shop.domain.articles.service.ArticleService
import com.jotoai.voenix.shop.domain.images.config.StoragePathConfiguration
import com.jotoai.voenix.shop.domain.images.service.StoragePathService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@WebMvcTest(PublicMugController::class)
@Import(SecurityConfig::class)
class PublicMugControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var articleService: ArticleService

    @MockitoBean
    private lateinit var storagePathService: StoragePathService

    @MockitoBean
    private lateinit var storagePathConfiguration: StoragePathConfiguration

    @Test
    fun `should return list of public mugs without authentication`() {
        // Given
        val mugs =
            listOf(
                PublicMugDto(
                    id = 1,
                    name = "Classic Mug",
                    price = 12.99,
                    image = "/api/admin/images/articles/mugs/variant-example-images/mug1.jpg",
                    fillingQuantity = "300ml",
                    descriptionShort = "A classic ceramic mug",
                    descriptionLong = "High quality ceramic mug suitable for hot beverages",
                    heightMm = 95,
                    diameterMm = 82,
                    printTemplateWidthMm = 200,
                    printTemplateHeightMm = 85,
                    dishwasherSafe = true,
                    variants =
                        listOf(
                            PublicMugVariantDto(
                                id = 1,
                                mugId = 1,
                                colorCode = "#FFFFFF",
                                exampleImageUrl = "/images/articles/mugs/variant-example-images/mug1.jpg",
                                articleVariantNumber = "MUG-001-WHT",
                                isDefault = true,
                                exampleImageFilename = "mug1.jpg",
                                createdAt = null,
                                updatedAt = null,
                            ),
                            PublicMugVariantDto(
                                id = 2,
                                mugId = 1,
                                colorCode = "#000000",
                                exampleImageUrl = "/images/articles/mugs/variant-example-images/mug1-black.jpg",
                                articleVariantNumber = "MUG-001-BLK",
                                isDefault = false,
                                exampleImageFilename = "mug1-black.jpg",
                                createdAt = null,
                                updatedAt = null,
                            ),
                        ),
                ),
                PublicMugDto(
                    id = 2,
                    name = "Travel Mug",
                    price = 18.99,
                    image = null,
                    fillingQuantity = "450ml",
                    descriptionShort = "Insulated travel mug",
                    descriptionLong = "Keep your beverages hot or cold on the go",
                    heightMm = 160,
                    diameterMm = 75,
                    printTemplateWidthMm = 180,
                    printTemplateHeightMm = 120,
                    dishwasherSafe = false,
                    variants = emptyList(),
                ),
            )

        `when`(articleService.findPublicMugs()).thenReturn(mugs)

        // When & Then
        mockMvc
            .get("/api/mugs")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].id") { value(1) }
                jsonPath("$[0].name") { value("Classic Mug") }
                jsonPath("$[0].price") { value(12.99) }
                jsonPath("$[0].dishwasherSafe") { value(true) }
                jsonPath("$[0].variants") { isArray() }
                jsonPath("$[0].variants[0].id") { value(1) }
                jsonPath("$[0].variants[0].colorCode") { value("#FFFFFF") }
                jsonPath("$[0].variants[0].isDefault") { value(true) }
                jsonPath("$[0].variants[1].id") { value(2) }
                jsonPath("$[0].variants[1].colorCode") { value("#000000") }
                jsonPath("$[0].variants[1].isDefault") { value(false) }
                jsonPath("$[1].id") { value(2) }
                jsonPath("$[1].name") { value("Travel Mug") }
                jsonPath("$[1].price") { value(18.99) }
                jsonPath("$[1].dishwasherSafe") { value(false) }
                jsonPath("$[1].variants") { isArray() }
                jsonPath("$[1].variants") { isEmpty() }
            }
    }

    @Test
    fun `should return empty list when no mugs available`() {
        // Given
        `when`(articleService.findPublicMugs()).thenReturn(emptyList())

        // When & Then
        mockMvc
            .get("/api/mugs")
            .andExpect {
                status { isOk() }
                jsonPath("$") { isArray() }
                jsonPath("$") { isEmpty() }
            }
    }
}
