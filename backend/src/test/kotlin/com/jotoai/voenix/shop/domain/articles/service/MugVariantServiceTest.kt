package com.jotoai.voenix.shop.domain.articles.service

import com.jotoai.voenix.shop.article.ArticleType
import com.jotoai.voenix.shop.article.MugArticleVariantDto
import com.jotoai.voenix.shop.article.internal.assembler.MugArticleVariantAssembler
import com.jotoai.voenix.shop.article.internal.assembler.MugWithVariantsSummaryAssembler
import com.jotoai.voenix.shop.article.internal.dto.CreateMugArticleVariantRequest
import com.jotoai.voenix.shop.article.internal.entity.Article
import com.jotoai.voenix.shop.article.internal.entity.ArticleCategory
import com.jotoai.voenix.shop.article.internal.entity.MugArticleVariant
import com.jotoai.voenix.shop.article.internal.exception.ArticleNotFoundException
import com.jotoai.voenix.shop.article.internal.repository.ArticleRepository
import com.jotoai.voenix.shop.article.internal.repository.MugArticleVariantRepository
import com.jotoai.voenix.shop.image.ImageService
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional

class MugVariantServiceTest {
    private lateinit var articleRepository: ArticleRepository
    private lateinit var mugVariantRepository: MugArticleVariantRepository
    private lateinit var imageService: ImageService
    private lateinit var mugArticleVariantAssembler: MugArticleVariantAssembler
    private lateinit var mugWithVariantsSummaryAssembler: MugWithVariantsSummaryAssembler
    private lateinit var mugVariantService: com.jotoai.voenix.shop.article.internal.service.MugVariantServiceImpl

    private lateinit var testArticle: Article
    private lateinit var testCategory: ArticleCategory

    @BeforeEach
    fun setUp() {
        articleRepository = mockk()
        mugVariantRepository = mockk()
        imageService = mockk()
        mugArticleVariantAssembler = mockk()
        mugWithVariantsSummaryAssembler = mockk()
        mugVariantService =
            com.jotoai.voenix.shop.article.internal.service.MugVariantServiceImpl(
                articleRepository,
                mugVariantRepository,
                imageService,
                mugArticleVariantAssembler,
                mugWithVariantsSummaryAssembler,
            )

        // Setup test data
        testCategory =
            ArticleCategory(
                id = 1L,
                name = "Test Category",
                description = "Test Description",
            )

        testArticle =
            Article(
                id = 1L,
                articleType = ArticleType.MUG,
                name = "Test Mug",
                descriptionShort = "Test Description Short",
                descriptionLong = "Test Description Long",
                active = true,
                category = testCategory,
                supplierArticleName = "Test Supplier Mug",
                supplierArticleNumber = "TSM-001",
            )
    }

    @Test
    fun `create should set first variant as default even if not requested`() {
        // Given
        val request =
            CreateMugArticleVariantRequest(
                insideColorCode = "#ffffff",
                outsideColorCode = "#000000",
                name = "Black & White",
                articleVariantNumber = "BW-001",
                isDefault = false, // Not requesting default
            )

        every { articleRepository.findById(1L) } returns Optional.of(testArticle)
        every { mugVariantRepository.findByArticleId(1L) } returns emptyList() // No existing variants
        every { mugVariantRepository.unsetDefaultForArticle(1L) } just Runs

        val savedVariant =
            MugArticleVariant(
                id = 1L,
                article = testArticle,
                insideColorCode = request.insideColorCode,
                outsideColorCode = request.outsideColorCode,
                name = request.name,
                articleVariantNumber = request.articleVariantNumber,
                isDefault = true, // Should be set to true
            )

        every { mugVariantRepository.save(any<MugArticleVariant>()) } returns savedVariant

        val expectedDto =
            MugArticleVariantDto(
                id = 1L,
                articleId = 1L,
                insideColorCode = request.insideColorCode,
                outsideColorCode = request.outsideColorCode,
                name = request.name,
                exampleImageUrl = null,
                articleVariantNumber = request.articleVariantNumber,
                isDefault = true,
                active = true,
                createdAt = null,
                updatedAt = null,
            )
        every { mugArticleVariantAssembler.toDto(savedVariant) } returns expectedDto

        // When
        val result = mugVariantService.create(1L, request)

        // Then
        assertTrue(result.isDefault)
        val variantCaptor = slot<MugArticleVariant>()
        verify { mugVariantRepository.save(capture(variantCaptor)) }
        assertTrue(variantCaptor.captured.isDefault)
    }

    @Test
    fun `create should unset existing default when creating new default`() {
        // Given
        val request =
            CreateMugArticleVariantRequest(
                insideColorCode = "#ffffff",
                outsideColorCode = "#ff0000",
                name = "Red & White",
                articleVariantNumber = "RW-001",
                isDefault = true,
            )

        val existingVariant =
            MugArticleVariant(
                id = 1L,
                article = testArticle,
                insideColorCode = "#ffffff",
                outsideColorCode = "#000000",
                name = "Black & White",
                isDefault = true,
            )

        every { articleRepository.findById(1L) } returns Optional.of(testArticle)
        every { mugVariantRepository.findByArticleId(1L) } returns listOf(existingVariant)
        every { mugVariantRepository.unsetDefaultForArticle(1L) } just Runs

        val savedVariant =
            MugArticleVariant(
                id = 2L,
                article = testArticle,
                insideColorCode = request.insideColorCode,
                outsideColorCode = request.outsideColorCode,
                name = request.name,
                articleVariantNumber = request.articleVariantNumber,
                isDefault = true,
            )

        every { mugVariantRepository.save(any<MugArticleVariant>()) } returns savedVariant

        val expectedDto =
            MugArticleVariantDto(
                id = 2L,
                articleId = 1L,
                insideColorCode = request.insideColorCode,
                outsideColorCode = request.outsideColorCode,
                name = request.name,
                exampleImageUrl = null,
                articleVariantNumber = request.articleVariantNumber,
                isDefault = true,
                active = true,
                createdAt = null,
                updatedAt = null,
            )
        every { mugArticleVariantAssembler.toDto(savedVariant) } returns expectedDto

        // When
        val result = mugVariantService.create(1L, request)

        // Then
        assertTrue(result.isDefault)
        verify { mugVariantRepository.unsetDefaultForArticle(1L) }
    }

    @Test
    fun `update should handle setting new default correctly`() {
        // Given
        val existingVariant =
            MugArticleVariant(
                id = 1L,
                article = testArticle,
                insideColorCode = "#ffffff",
                outsideColorCode = "#000000",
                name = "Black & White",
                isDefault = false,
            )

        val request =
            CreateMugArticleVariantRequest(
                insideColorCode = "#ffffff",
                outsideColorCode = "#ff0000",
                name = "Red & White Updated",
                articleVariantNumber = "RW-002",
                isDefault = true,
            )

        every { mugVariantRepository.findByIdWithArticle(1L) } returns Optional.of(existingVariant)
        every { mugVariantRepository.save(any<MugArticleVariant>()) } answers { firstArg() }
        every { mugVariantRepository.unsetDefaultForArticle(1L) } just Runs

        val expectedDto =
            MugArticleVariantDto(
                id = 1L,
                articleId = 1L,
                insideColorCode = request.insideColorCode,
                outsideColorCode = request.outsideColorCode,
                name = request.name,
                exampleImageUrl = null,
                articleVariantNumber = request.articleVariantNumber,
                isDefault = true,
                active = true,
                createdAt = null,
                updatedAt = null,
            )
        every { mugArticleVariantAssembler.toDto(existingVariant) } returns expectedDto

        // When
        val result = mugVariantService.update(1L, request)

        // Then
        assertTrue(result.isDefault)
        verify { mugVariantRepository.unsetDefaultForArticle(1L) }
    }

    @Test
    fun `update should assign new default when unsetting current default`() {
        // Given
        val currentDefaultVariant =
            MugArticleVariant(
                id = 1L,
                article = testArticle,
                insideColorCode = "#ffffff",
                outsideColorCode = "#000000",
                name = "Black & White",
                isDefault = true,
            )

        val otherVariant =
            MugArticleVariant(
                id = 2L,
                article = testArticle,
                insideColorCode = "#ffffff",
                outsideColorCode = "#ff0000",
                name = "Red & White",
                isDefault = false,
            )

        val request =
            CreateMugArticleVariantRequest(
                insideColorCode = "#ffffff",
                outsideColorCode = "#000000",
                name = "Black & White Updated",
                articleVariantNumber = "BW-002",
                isDefault = false, // Unsetting default
            )

        every { mugVariantRepository.findByIdWithArticle(1L) } returns Optional.of(currentDefaultVariant)
        every { mugVariantRepository.findByArticleId(1L) } returns listOf(currentDefaultVariant, otherVariant)
        every { mugVariantRepository.save(any<MugArticleVariant>()) } answers { firstArg() }

        val expectedDto =
            MugArticleVariantDto(
                id = 1L,
                articleId = 1L,
                insideColorCode = request.insideColorCode,
                outsideColorCode = request.outsideColorCode,
                name = request.name,
                exampleImageUrl = null,
                articleVariantNumber = request.articleVariantNumber,
                isDefault = false,
                active = true,
                createdAt = null,
                updatedAt = null,
            )
        every { mugArticleVariantAssembler.toDto(any<MugArticleVariant>()) } returns expectedDto

        // When
        mugVariantService.update(1L, request)

        // Then
        verify(exactly = 2) { mugVariantRepository.save(any<MugArticleVariant>()) }
        // Save both the updated variant and the new default
    }

    @Test
    fun `delete should assign new default when deleting default variant`() {
        // Given
        val defaultVariant =
            MugArticleVariant(
                id = 1L,
                article = testArticle,
                insideColorCode = "#ffffff",
                outsideColorCode = "#000000",
                name = "Black & White",
                isDefault = true,
            )

        val otherVariant =
            MugArticleVariant(
                id = 2L,
                article = testArticle,
                insideColorCode = "#ffffff",
                outsideColorCode = "#ff0000",
                name = "Red & White",
                isDefault = false,
            )

        every { mugVariantRepository.findByIdWithArticle(1L) } returns Optional.of(defaultVariant)
        every { mugVariantRepository.findByArticleId(1L) } returns listOf(otherVariant)
        every { mugVariantRepository.save(any<MugArticleVariant>()) } answers { firstArg() }
        every { mugVariantRepository.deleteById(1L) } just Runs

        // When
        mugVariantService.delete(1L)

        // Then
        verify { mugVariantRepository.deleteById(1L) }
        val variantCaptor = slot<MugArticleVariant>()
        verify { mugVariantRepository.save(capture(variantCaptor)) }
        assertEquals(2L, variantCaptor.captured.id)
        assertTrue(variantCaptor.captured.isDefault)
    }

    @Test
    fun `delete should not assign new default when no variants remain`() {
        // Given
        val defaultVariant =
            MugArticleVariant(
                id = 1L,
                article = testArticle,
                insideColorCode = "#ffffff",
                outsideColorCode = "#000000",
                name = "Black & White",
                isDefault = true,
            )

        every { mugVariantRepository.findByIdWithArticle(1L) } returns Optional.of(defaultVariant)
        every { mugVariantRepository.findByArticleId(1L) } returns emptyList()
        every { mugVariantRepository.deleteById(1L) } just Runs

        // When
        mugVariantService.delete(1L)

        // Then
        verify { mugVariantRepository.deleteById(1L) }
        verify(exactly = 0) { mugVariantRepository.save(any<MugArticleVariant>()) }
    }

    @Test
    fun `create should throw ArticleNotFoundException when article not found`() {
        // Given
        val request =
            CreateMugArticleVariantRequest(
                insideColorCode = "#ffffff",
                outsideColorCode = "#000000",
                name = "Black & White",
                articleVariantNumber = "BW-001",
                isDefault = false,
            )

        every { articleRepository.findById(999L) } returns Optional.empty()

        // When/Then
        assertThrows<ArticleNotFoundException> {
            mugVariantService.create(999L, request)
        }
    }

    @Test
    fun `update should throw ArticleNotFoundException when variant not found`() {
        // Given
        val request =
            CreateMugArticleVariantRequest(
                insideColorCode = "#ffffff",
                outsideColorCode = "#000000",
                name = "Black & White",
                articleVariantNumber = "BW-001",
                isDefault = false,
            )

        every { mugVariantRepository.findByIdWithArticle(999L) } returns Optional.empty()

        // When/Then
        assertThrows<ArticleNotFoundException> {
            mugVariantService.update(999L, request)
        }
    }
}
