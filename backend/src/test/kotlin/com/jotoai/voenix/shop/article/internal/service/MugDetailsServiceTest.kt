package com.jotoai.voenix.shop.article.internal.service

import com.jotoai.voenix.shop.article.api.dto.CreateMugDetailsRequest
import com.jotoai.voenix.shop.article.api.dto.UpdateMugDetailsRequest
import com.jotoai.voenix.shop.article.api.enums.ArticleType
import com.jotoai.voenix.shop.article.internal.categories.entity.ArticleCategory
import com.jotoai.voenix.shop.article.internal.entity.Article
import com.jotoai.voenix.shop.article.internal.entity.MugArticleDetails
import com.jotoai.voenix.shop.article.internal.repository.MugArticleDetailsRepository
import io.mockk.any
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("MugDetailsService Tests")
class MugDetailsServiceTest {
    private lateinit var mugDetailsRepository: MugArticleDetailsRepository
    private lateinit var mugDetailsService: MugDetailsService

    @BeforeEach
    fun setUp() {
        mugDetailsRepository = mockk()
        mugDetailsService = MugDetailsService(mugDetailsRepository)
    }

    private fun createTestArticle(id: Long = 1L): Article {
        val category = mockk<ArticleCategory>()
        return Article(
            id = id,
            name = "Test Mug",
            descriptionShort = "Test mug description",
            descriptionLong = "Test mug long description",
            articleType = ArticleType.MUG,
            category = category,
        )
    }

    @Nested
    @DisplayName("Create Mug Details")
    inner class CreateMugDetailsTests {
        @Test
        fun `should create mug details with all fields including document format`() {
            // Given
            val article = createTestArticle()
            val request =
                CreateMugDetailsRequest(
                    heightMm = 95,
                    diameterMm = 82,
                    printTemplateWidthMm = 200,
                    printTemplateHeightMm = 80,
                    documentFormatWidthMm = 250,
                    documentFormatHeightMm = 120,
                    documentFormatMarginBottomMm = 10,
                    fillingQuantity = "330ml",
                    dishwasherSafe = true,
                )

            val savedDetailsCaptor = slot<MugArticleDetails>()
            every { mugDetailsRepository.saveAndFlush(capture(savedDetailsCaptor)) } answers { firstArg() }

            // When
            val result = mugDetailsService.create(article, request)

            // Then
            verify(exactly = 1) { mugDetailsRepository.saveAndFlush(any()) }

            val savedDetails = savedDetailsCaptor.value
            assertThat(savedDetails.articleId).isEqualTo(1L)
            assertThat(savedDetails.heightMm).isEqualTo(95)
            assertThat(savedDetails.diameterMm).isEqualTo(82)
            assertThat(savedDetails.printTemplateWidthMm).isEqualTo(200)
            assertThat(savedDetails.printTemplateHeightMm).isEqualTo(80)
            assertThat(savedDetails.documentFormatWidthMm).isEqualTo(250)
            assertThat(savedDetails.documentFormatHeightMm).isEqualTo(120)
            assertThat(savedDetails.documentFormatMarginBottomMm).isEqualTo(10)
            assertThat(savedDetails.fillingQuantity).isEqualTo("330ml")
            assertThat(savedDetails.dishwasherSafe).isTrue()
        }

        @Test
        fun `should create mug details without optional document format fields`() {
            // Given
            val article = createTestArticle()
            val request =
                CreateMugDetailsRequest(
                    heightMm = 95,
                    diameterMm = 82,
                    printTemplateWidthMm = 200,
                    printTemplateHeightMm = 80,
                    documentFormatWidthMm = null,
                    documentFormatHeightMm = null,
                    documentFormatMarginBottomMm = null,
                    fillingQuantity = null,
                    dishwasherSafe = true,
                )

            val savedDetailsCaptor = slot<MugArticleDetails>()
            every { mugDetailsRepository.saveAndFlush(capture(savedDetailsCaptor)) } answers { firstArg() }

            // When
            val result = mugDetailsService.create(article, request)

            // Then
            val savedDetails = savedDetailsCaptor.value
            assertThat(savedDetails.documentFormatWidthMm).isNull()
            assertThat(savedDetails.documentFormatHeightMm).isNull()
            assertThat(savedDetails.documentFormatMarginBottomMm).isNull()
        }

        @Test
        fun `should reject document format width less than or equal to print template width`() {
            // Given
            val article = createTestArticle()
            val request =
                CreateMugDetailsRequest(
                    heightMm = 95,
                    diameterMm = 82,
                    printTemplateWidthMm = 200,
                    printTemplateHeightMm = 80,
                    documentFormatWidthMm = 200, // Equal to print template width
                    documentFormatHeightMm = 120,
                    documentFormatMarginBottomMm = 10,
                    dishwasherSafe = true,
                )

            // When/Then
            assertThatThrownBy { mugDetailsService.create(article, request) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("Document format width")
                .hasMessageContaining("must be greater than")
                .hasMessageContaining("print template width")
        }

        @Test
        fun `should reject document format height less than or equal to print template height`() {
            // Given
            val article = createTestArticle()
            val request =
                CreateMugDetailsRequest(
                    heightMm = 95,
                    diameterMm = 82,
                    printTemplateWidthMm = 200,
                    printTemplateHeightMm = 80,
                    documentFormatWidthMm = 250,
                    documentFormatHeightMm = 80, // Equal to print template height
                    documentFormatMarginBottomMm = 10,
                    dishwasherSafe = true,
                )

            // When/Then
            assertThatThrownBy { mugDetailsService.create(article, request) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("Document format height")
                .hasMessageContaining("must be greater than")
                .hasMessageContaining("print template height")
        }

        @ParameterizedTest
        @CsvSource(
            "199, 120", // Width too small
            "200, 120", // Width equal
            "250, 79", // Height too small
            "250, 80", // Height equal
        )
        fun `should reject invalid document format dimensions`(
            docWidth: Int,
            docHeight: Int,
        ) {
            // Given
            val article = createTestArticle()
            val request =
                CreateMugDetailsRequest(
                    heightMm = 95,
                    diameterMm = 82,
                    printTemplateWidthMm = 200,
                    printTemplateHeightMm = 80,
                    documentFormatWidthMm = docWidth,
                    documentFormatHeightMm = docHeight,
                    documentFormatMarginBottomMm = 10,
                    dishwasherSafe = true,
                )

            // When/Then
            assertThatThrownBy { mugDetailsService.create(article, request) }
                .isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    @Nested
    @DisplayName("Update Mug Details")
    inner class UpdateMugDetailsTests {
        @Test
        fun `should update existing mug details with document format fields`() {
            // Given
            val article = createTestArticle()
            val existingDetails =
                MugArticleDetails(
                    articleId = 1L,
                    heightMm = 90,
                    diameterMm = 80,
                    printTemplateWidthMm = 190,
                    printTemplateHeightMm = 75,
                    documentFormatWidthMm = null,
                    documentFormatHeightMm = null,
                    documentFormatMarginBottomMm = null,
                )

            val request =
                UpdateMugDetailsRequest(
                    heightMm = 95,
                    diameterMm = 82,
                    printTemplateWidthMm = 200,
                    printTemplateHeightMm = 80,
                    documentFormatWidthMm = 250,
                    documentFormatHeightMm = 120,
                    documentFormatMarginBottomMm = 15,
                    fillingQuantity = "330ml",
                    dishwasherSafe = false,
                )

            every { mugDetailsRepository.findByArticleId(1L) } returns existingDetails
            every { mugDetailsRepository.saveAndFlush(any()) } answers { firstArg() }

            // When
            val result = mugDetailsService.update(article, request)

            // Then
            verify(exactly = 1) { mugDetailsRepository.findByArticleId(1L) }
            verify(exactly = 1) { mugDetailsRepository.saveAndFlush(existingDetails) }

            assertThat(existingDetails.heightMm).isEqualTo(95)
            assertThat(existingDetails.diameterMm).isEqualTo(82)
            assertThat(existingDetails.printTemplateWidthMm).isEqualTo(200)
            assertThat(existingDetails.printTemplateHeightMm).isEqualTo(80)
            assertThat(existingDetails.documentFormatWidthMm).isEqualTo(250)
            assertThat(existingDetails.documentFormatHeightMm).isEqualTo(120)
            assertThat(existingDetails.documentFormatMarginBottomMm).isEqualTo(15)
            assertThat(existingDetails.fillingQuantity).isEqualTo("330ml")
            assertThat(existingDetails.dishwasherSafe).isFalse()
        }

        @Test
        fun `should create new mug details if not exists during update`() {
            // Given
            val article = createTestArticle()
            val request =
                UpdateMugDetailsRequest(
                    heightMm = 95,
                    diameterMm = 82,
                    printTemplateWidthMm = 200,
                    printTemplateHeightMm = 80,
                    documentFormatWidthMm = 250,
                    documentFormatHeightMm = 120,
                    documentFormatMarginBottomMm = 10,
                    fillingQuantity = "330ml",
                    dishwasherSafe = true,
                )

            every { mugDetailsRepository.findByArticleId(1L) } returns null
            every { mugDetailsRepository.saveAndFlush(any()) } answers { firstArg() }

            // When
            val result = mugDetailsService.update(article, request)

            // Then
            verify(mugDetailsRepository, times(1)).findByArticleId(1L)
            verify(mugDetailsRepository, times(1)).saveAndFlush(any(MugArticleDetails::class.java))
        }

        @Test
        fun `should clear document format fields when updating with null values`() {
            // Given
            val article = createTestArticle()
            val existingDetails =
                MugArticleDetails(
                    articleId = 1L,
                    heightMm = 95,
                    diameterMm = 82,
                    printTemplateWidthMm = 200,
                    printTemplateHeightMm = 80,
                    documentFormatWidthMm = 250,
                    documentFormatHeightMm = 120,
                    documentFormatMarginBottomMm = 10,
                )

            val request =
                UpdateMugDetailsRequest(
                    heightMm = 95,
                    diameterMm = 82,
                    printTemplateWidthMm = 200,
                    printTemplateHeightMm = 80,
                    documentFormatWidthMm = null,
                    documentFormatHeightMm = null,
                    documentFormatMarginBottomMm = null,
                    dishwasherSafe = true,
                )

            every { mugDetailsRepository.findByArticleId(1L) } returns existingDetails
            every { mugDetailsRepository.saveAndFlush(any()) } answers { firstArg() }

            // When
            mugDetailsService.update(article, request)

            // Then
            assertThat(existingDetails.documentFormatWidthMm).isNull()
            assertThat(existingDetails.documentFormatHeightMm).isNull()
            assertThat(existingDetails.documentFormatMarginBottomMm).isNull()
        }

        @Test
        fun `should validate document format during update`() {
            // Given
            val article = createTestArticle()
            val existingDetails =
                MugArticleDetails(
                    articleId = 1L,
                    heightMm = 95,
                    diameterMm = 82,
                    printTemplateWidthMm = 200,
                    printTemplateHeightMm = 80,
                )

            val request =
                UpdateMugDetailsRequest(
                    heightMm = 95,
                    diameterMm = 82,
                    printTemplateWidthMm = 200,
                    printTemplateHeightMm = 80,
                    documentFormatWidthMm = 150, // Less than print template
                    documentFormatHeightMm = 120,
                    documentFormatMarginBottomMm = 10,
                    dishwasherSafe = true,
                )

            every { mugDetailsRepository.findByArticleId(1L) } returns existingDetails

            // When/Then
            assertThatThrownBy { mugDetailsService.update(article, request) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("Document format width")
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Values")
    inner class EdgeCaseTests {
        @Test
        fun `should accept document format one millimeter larger than print template`() {
            // Given
            val article = createTestArticle()
            val request =
                CreateMugDetailsRequest(
                    heightMm = 95,
                    diameterMm = 82,
                    printTemplateWidthMm = 200,
                    printTemplateHeightMm = 80,
                    documentFormatWidthMm = 201, // Just 1mm larger
                    documentFormatHeightMm = 81, // Just 1mm larger
                    documentFormatMarginBottomMm = 0, // Minimum margin
                    dishwasherSafe = true,
                )

            every { mugDetailsRepository.saveAndFlush(any()) } answers { firstArg() }

            // When/Then - should not throw
            val result = mugDetailsService.create(article, request)
            assertThat(result.documentFormatWidthMm).isEqualTo(201)
            assertThat(result.documentFormatHeightMm).isEqualTo(81)
            assertThat(result.documentFormatMarginBottomMm).isEqualTo(0)
        }

        @Test
        fun `should handle very large document format values`() {
            // Given
            val article = createTestArticle()
            val request =
                CreateMugDetailsRequest(
                    heightMm = 95,
                    diameterMm = 82,
                    printTemplateWidthMm = 200,
                    printTemplateHeightMm = 80,
                    documentFormatWidthMm = 10000, // Very large
                    documentFormatHeightMm = 10000, // Very large
                    documentFormatMarginBottomMm = 1000, // Large margin
                    dishwasherSafe = true,
                )

            every { mugDetailsRepository.saveAndFlush(any()) } answers { firstArg() }

            // When - Currently no max validation, so this should pass
            val result = mugDetailsService.create(article, request)

            // Then
            assertThat(result.documentFormatWidthMm).isEqualTo(10000)
            assertThat(result.documentFormatHeightMm).isEqualTo(10000)
            assertThat(result.documentFormatMarginBottomMm).isEqualTo(1000)
            // Note: This test highlights the need for maximum value validation
        }

        @Test
        fun `should validate only provided document format dimensions`() {
            // Given - only width provided, height is null
            val article = createTestArticle()
            val request =
                CreateMugDetailsRequest(
                    heightMm = 95,
                    diameterMm = 82,
                    printTemplateWidthMm = 200,
                    printTemplateHeightMm = 80,
                    documentFormatWidthMm = 199, // Invalid: less than template
                    documentFormatHeightMm = null, // Not provided
                    documentFormatMarginBottomMm = 10,
                    dishwasherSafe = true,
                )

            // When/Then - should validate only the width
            assertThatThrownBy { mugDetailsService.create(article, request) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("Document format width")
        }
    }
}
