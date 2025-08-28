package com.jotoai.voenix.shop.article.web

import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.article.api.dto.PublicMugDto
import com.jotoai.voenix.shop.article.api.dto.PublicMugVariantDto
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import com.jotoai.voenix.shop.article.api.ArticleFacade

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PublicMugControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var articleQueryService: ArticleQueryService

    @MockkBean
    private lateinit var articleFacade: ArticleFacade

    @Test
    fun `should return list of public mugs without authentication`() {
        // Given
        val mugs = listOf(createClassicMug(), createTravelMug())
        every { articleQueryService.findPublicMugs() } returns mugs

        // When & Then
        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/mugs"))
            .andExpect(status().isOk)
            // Classic Mug assertions
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Classic Mug"))
            .andExpect(jsonPath("$[0].price").value(12.99))
            .andExpect(jsonPath("$[0].dishwasherSafe").value(true))
            .andExpect(jsonPath("$[0].variants").isArray)
            .andExpect(jsonPath("$[0].variants[0].id").value(1))
            .andExpect(jsonPath("$[0].variants[0].colorCode").value("#FFFFFF"))
            .andExpect(jsonPath("$[0].variants[0].isDefault").value(true))
            .andExpect(jsonPath("$[0].variants[1].id").value(2))
            .andExpect(jsonPath("$[0].variants[1].colorCode").value("#000000"))
            .andExpect(jsonPath("$[0].variants[1].isDefault").value(false))
            // Travel Mug assertions
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].name").value("Travel Mug"))
            .andExpect(jsonPath("$[1].price").value(18.99))
            .andExpect(jsonPath("$[1].dishwasherSafe").value(false))
            .andExpect(jsonPath("$[1].variants").isArray)
            .andExpect(jsonPath("$[1].variants").isEmpty)
    }

    private fun createClassicMug() =
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
            variants = createClassicMugVariants(),
        )

    private fun createClassicMugVariants() =
        listOf(
            PublicMugVariantDto(
                id = 1,
                mugId = 1,
                colorCode = "#FFFFFF",
                name = "White",
                exampleImageUrl = "/images/articles/mugs/variant-example-images/mug1.jpg",
                articleVariantNumber = "MUG-001-WHT",
                isDefault = true,
                active = true,
                exampleImageFilename = "mug1.jpg",
                createdAt = null,
                updatedAt = null,
            ),
            PublicMugVariantDto(
                id = 2,
                mugId = 1,
                colorCode = "#000000",
                name = "Black",
                exampleImageUrl = "/images/articles/mugs/variant-example-images/mug1-black.jpg",
                articleVariantNumber = "MUG-001-BLK",
                isDefault = false,
                active = true,
                exampleImageFilename = "mug1-black.jpg",
                createdAt = null,
                updatedAt = null,
            ),
        )

    private fun createTravelMug() =
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
        )

    @Test
    fun `should return empty list when no mugs available`() {
        // Given
        every { articleQueryService.findPublicMugs() } returns emptyList()

        // When & Then
        mockMvc
            .perform(MockMvcRequestBuilders.get("/api/mugs"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$").isEmpty)
    }
}
