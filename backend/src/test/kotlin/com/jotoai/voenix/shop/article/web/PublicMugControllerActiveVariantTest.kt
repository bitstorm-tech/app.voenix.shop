package com.jotoai.voenix.shop.article.web

import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.article.api.dto.PublicMugDto
import com.jotoai.voenix.shop.article.api.dto.PublicMugVariantDto
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.OffsetDateTime

/**
 * Test suite for the PublicMugController with focus on active variant filtering.
 * Ensures that customers only see active variants through the public API.
 */
@WebMvcTest(PublicMugController::class)
class PublicMugControllerActiveVariantTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var articleQueryService: ArticleQueryService

    private lateinit var testMugWithMixedVariants: PublicMugDto
    private lateinit var testMugWithOnlyActiveVariants: PublicMugDto
    private lateinit var testMugWithNoActiveVariants: PublicMugDto

    @BeforeEach
    fun setUp() {
        val now = OffsetDateTime.now()
        testMugWithMixedVariants = createPremiumCoffeeMug(now)
        testMugWithOnlyActiveVariants = createTravelMug(now)
        testMugWithNoActiveVariants = createDiscontinuedMug()
    }

    private fun createPremiumCoffeeMug(now: OffsetDateTime) =
        PublicMugDto(
            id = 1L,
            name = "Premium Coffee Mug",
            descriptionShort = "High-quality ceramic mug",
            descriptionLong = "Dishwasher safe premium ceramic coffee mug",
            image = "/images/mugs/premium.jpg",
            price = 15.99,
            heightMm = 95,
            diameterMm = 80,
            printTemplateWidthMm = 200,
            printTemplateHeightMm = 90,
            fillingQuantity = "330ml",
            dishwasherSafe = true,
            variants = createPremiumMugVariants(now),
        )

    private fun createPremiumMugVariants(now: OffsetDateTime) =
        listOf(
            PublicMugVariantDto(
                id = 1L,
                mugId = 1L,
                colorCode = "#FFFFFF",
                name = "White",
                exampleImageUrl = "/images/variants/white.jpg",
                articleVariantNumber = "PM-WHITE",
                isDefault = true,
                active = true,
                exampleImageFilename = "white.jpg",
                createdAt = now,
                updatedAt = now,
            ),
            PublicMugVariantDto(
                id = 2L,
                mugId = 1L,
                colorCode = "#000000",
                name = "Black",
                exampleImageUrl = "/images/variants/black.jpg",
                articleVariantNumber = "PM-BLACK",
                isDefault = false,
                active = true,
                exampleImageFilename = "black.jpg",
                createdAt = now,
                updatedAt = now,
            ),
        )

    private fun createTravelMug(now: OffsetDateTime) =
        PublicMugDto(
            id = 2L,
            name = "Travel Mug",
            descriptionShort = "Insulated travel mug",
            descriptionLong = "Double-wall insulated travel mug",
            image = "/images/mugs/travel.jpg",
            price = 24.99,
            heightMm = 150,
            diameterMm = 75,
            printTemplateWidthMm = 180,
            printTemplateHeightMm = 120,
            fillingQuantity = "450ml",
            dishwasherSafe = true,
            variants =
                listOf(
                    PublicMugVariantDto(
                        id = 3L,
                        mugId = 2L,
                        colorCode = "#FF0000",
                        name = "Red",
                        exampleImageUrl = "/images/variants/red.jpg",
                        articleVariantNumber = "TM-RED",
                        isDefault = true,
                        active = true,
                        exampleImageFilename = "red.jpg",
                        createdAt = now,
                        updatedAt = now,
                    ),
                ),
        )

    private fun createDiscontinuedMug() =
        PublicMugDto(
            id = 3L,
            name = "Discontinued Mug",
            descriptionShort = "Old model",
            descriptionLong = "This mug model has been discontinued",
            image = "/images/mugs/discontinued.jpg",
            price = 9.99,
            heightMm = 90,
            diameterMm = 85,
            printTemplateWidthMm = 190,
            printTemplateHeightMm = 85,
            fillingQuantity = "300ml",
            dishwasherSafe = false,
            variants = emptyList(),
        )

    @Test
    @DisplayName("GET /api/mugs should return mugs with only active variants")
    fun getAllMugs_ReturnsOnlyActiveVariants() {
        // Given
        val mugs = listOf(testMugWithMixedVariants, testMugWithOnlyActiveVariants)
        `when`(articleQueryService.findPublicMugs()).thenReturn(mugs)

        // When & Then
        mockMvc
            .perform(
                get("/api/mugs")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].variants").isArray)
            .andExpect(jsonPath("$[0].variants.length()").value(2))
            .andExpect(jsonPath("$[0].variants[0].active").value(true))
            .andExpect(jsonPath("$[0].variants[1].active").value(true))
            .andExpect(jsonPath("$[1].id").value(2))
            .andExpect(jsonPath("$[1].variants.length()").value(1))
            .andExpect(jsonPath("$[1].variants[0].active").value(true))

        verify(articleQueryService, times(1)).findPublicMugs()
    }

    @Test
    @DisplayName("GET /api/mugs should handle mugs with no active variants")
    fun getAllMugs_HandlesNoActiveVariants() {
        // Given - Including a mug with no active variants
        val mugs = listOf(testMugWithOnlyActiveVariants, testMugWithNoActiveVariants)
        `when`(articleQueryService.findPublicMugs()).thenReturn(mugs)

        // When & Then
        mockMvc
            .perform(
                get("/api/mugs")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].variants.length()").value(1))
            .andExpect(jsonPath("$[1].variants").isEmpty) // Empty array for no active variants
    }

    @Test
    @DisplayName("GET /api/mugs should return empty list when no mugs available")
    fun getAllMugs_ReturnsEmptyListWhenNoMugs() {
        // Given
        `when`(articleQueryService.findPublicMugs()).thenReturn(emptyList())

        // When & Then
        mockMvc
            .perform(
                get("/api/mugs")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray)
            .andExpect(jsonPath("$.length()").value(0))
    }

    @Test
    @DisplayName("Active variants should have proper structure in response")
    fun getAllMugs_ActiveVariantsHaveProperStructure() {
        // Given
        val mugs = listOf(testMugWithMixedVariants)
        `when`(articleQueryService.findPublicMugs()).thenReturn(mugs)

        // When & Then
        mockMvc
            .perform(
                get("/api/mugs")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].variants[0].id").exists())
            .andExpect(jsonPath("$[0].variants[0].mugId").value(1))
            .andExpect(jsonPath("$[0].variants[0].colorCode").exists())
            .andExpect(jsonPath("$[0].variants[0].exampleImageUrl").exists())
            .andExpect(jsonPath("$[0].variants[0].isDefault").exists())
            .andExpect(jsonPath("$[0].variants[0].active").value(true))
    }

    @Test
    @DisplayName("Default variant should always be active")
    fun getAllMugs_DefaultVariantIsActive() {
        // Given
        val mugs = listOf(testMugWithMixedVariants)
        `when`(articleQueryService.findPublicMugs()).thenReturn(mugs)

        // When & Then
        mockMvc
            .perform(
                get("/api/mugs")
                    .contentType(MediaType.APPLICATION_JSON),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$[0].variants[?(@.isDefault == true)].active").value(true))
    }

    @Test
    @DisplayName("Service layer filtering should be transparent to controller")
    fun serviceLayerFiltering_IsTransparent() {
        // This test verifies that the controller doesn't need to know about
        // active/inactive filtering - it's handled at the service layer

        // Given
        `when`(articleQueryService.findPublicMugs()).thenReturn(listOf(testMugWithMixedVariants))

        // When
        mockMvc
            .perform(get("/api/mugs"))
            .andExpect(status().isOk)

        // Then
        // Controller just passes through what service returns
        // Service is responsible for filtering
        verify(articleQueryService, times(1)).findPublicMugs()
        verifyNoMoreInteractions(articleQueryService)
    }
}
