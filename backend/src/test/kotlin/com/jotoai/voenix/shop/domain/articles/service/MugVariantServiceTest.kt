package com.jotoai.voenix.shop.domain.articles.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.articles.assembler.MugArticleVariantAssembler
import com.jotoai.voenix.shop.domain.articles.categories.entity.ArticleCategory
import com.jotoai.voenix.shop.domain.articles.dto.CreateMugArticleVariantRequest
import com.jotoai.voenix.shop.domain.articles.dto.MugArticleVariantDto
import com.jotoai.voenix.shop.domain.articles.entity.Article
import com.jotoai.voenix.shop.domain.articles.entity.MugArticleVariant
import com.jotoai.voenix.shop.domain.articles.enums.ArticleType
import com.jotoai.voenix.shop.domain.articles.repository.ArticleRepository
import com.jotoai.voenix.shop.domain.articles.repository.MugArticleVariantRepository
import com.jotoai.voenix.shop.image.api.ImageStorageService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.Optional

class MugVariantServiceTest {
    private lateinit var articleRepository: ArticleRepository
    private lateinit var mugVariantRepository: MugArticleVariantRepository
    private lateinit var imageStorageService: ImageStorageService
    private lateinit var mugArticleVariantAssembler: MugArticleVariantAssembler
    private lateinit var mugVariantService: MugVariantService

    private lateinit var testArticle: Article
    private lateinit var testCategory: ArticleCategory

    @BeforeEach
    fun setUp() {
        articleRepository = mock(ArticleRepository::class.java)
        mugVariantRepository = mock(MugArticleVariantRepository::class.java)
        imageStorageService = mock(ImageStorageService::class.java)
        mugArticleVariantAssembler = mock(MugArticleVariantAssembler::class.java)
        mugVariantService = MugVariantService(articleRepository, mugVariantRepository, imageStorageService, mugArticleVariantAssembler)

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

        `when`(articleRepository.findById(1L)).thenReturn(Optional.of(testArticle))
        `when`(mugVariantRepository.findByArticleId(1L)).thenReturn(emptyList()) // No existing variants

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

        `when`(mugVariantRepository.save(any(MugArticleVariant::class.java))).thenReturn(savedVariant)

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
                createdAt = null,
                updatedAt = null,
            )
        `when`(mugArticleVariantAssembler.toDto(savedVariant)).thenReturn(expectedDto)

        // When
        val result = mugVariantService.create(1L, request)

        // Then
        assertTrue(result.isDefault)
        val variantCaptor = ArgumentCaptor.forClass(MugArticleVariant::class.java)
        verify(mugVariantRepository).save(variantCaptor.capture())
        assertTrue(variantCaptor.value.isDefault)
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

        `when`(articleRepository.findById(1L)).thenReturn(Optional.of(testArticle))
        `when`(mugVariantRepository.findByArticleId(1L)).thenReturn(listOf(existingVariant))

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

        `when`(mugVariantRepository.save(any(MugArticleVariant::class.java))).thenReturn(savedVariant)

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
                createdAt = null,
                updatedAt = null,
            )
        `when`(mugArticleVariantAssembler.toDto(savedVariant)).thenReturn(expectedDto)

        // When
        val result = mugVariantService.create(1L, request)

        // Then
        assertTrue(result.isDefault)
        verify(mugVariantRepository).unsetDefaultForArticle(1L)
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

        `when`(mugVariantRepository.findByIdWithArticle(1L)).thenReturn(Optional.of(existingVariant))
        `when`(mugVariantRepository.save(any(MugArticleVariant::class.java))).thenAnswer { invocation ->
            invocation.arguments[0] as MugArticleVariant
        }

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
                createdAt = null,
                updatedAt = null,
            )
        `when`(mugArticleVariantAssembler.toDto(existingVariant)).thenReturn(expectedDto)

        // When
        val result = mugVariantService.update(1L, request)

        // Then
        assertTrue(result.isDefault)
        verify(mugVariantRepository).unsetDefaultForArticle(1L)
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

        `when`(mugVariantRepository.findByIdWithArticle(1L)).thenReturn(Optional.of(currentDefaultVariant))
        `when`(mugVariantRepository.findByArticleId(1L)).thenReturn(listOf(currentDefaultVariant, otherVariant))
        `when`(mugVariantRepository.save(any(MugArticleVariant::class.java))).thenAnswer { invocation ->
            invocation.arguments[0] as MugArticleVariant
        }

        // When
        mugVariantService.update(1L, request)

        // Then
        verify(mugVariantRepository, times(2)).save(any(MugArticleVariant::class.java)) // Save both the updated variant and the new default
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

        `when`(mugVariantRepository.findByIdWithArticle(1L)).thenReturn(Optional.of(defaultVariant))
        `when`(mugVariantRepository.findByArticleId(1L)).thenReturn(listOf(otherVariant))
        `when`(mugVariantRepository.save(any(MugArticleVariant::class.java))).thenAnswer { invocation ->
            invocation.arguments[0] as MugArticleVariant
        }

        // When
        mugVariantService.delete(1L)

        // Then
        verify(mugVariantRepository).deleteById(1L)
        val variantCaptor = ArgumentCaptor.forClass(MugArticleVariant::class.java)
        verify(mugVariantRepository).save(variantCaptor.capture())
        assertEquals(2L, variantCaptor.value.id)
        assertTrue(variantCaptor.value.isDefault)
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

        `when`(mugVariantRepository.findByIdWithArticle(1L)).thenReturn(Optional.of(defaultVariant))
        `when`(mugVariantRepository.findByArticleId(1L)).thenReturn(emptyList())

        // When
        mugVariantService.delete(1L)

        // Then
        verify(mugVariantRepository).deleteById(1L)
        verify(mugVariantRepository, times(0)).save(any(MugArticleVariant::class.java)) // No save should occur
    }

    @Test
    fun `create should throw ResourceNotFoundException when article not found`() {
        // Given
        val request =
            CreateMugArticleVariantRequest(
                insideColorCode = "#ffffff",
                outsideColorCode = "#000000",
                name = "Black & White",
                articleVariantNumber = "BW-001",
                isDefault = false,
            )

        `when`(articleRepository.findById(999L)).thenReturn(Optional.empty())

        // When/Then
        assertThrows<ResourceNotFoundException> {
            mugVariantService.create(999L, request)
        }
    }

    @Test
    fun `update should throw ResourceNotFoundException when variant not found`() {
        // Given
        val request =
            CreateMugArticleVariantRequest(
                insideColorCode = "#ffffff",
                outsideColorCode = "#000000",
                name = "Black & White",
                articleVariantNumber = "BW-001",
                isDefault = false,
            )

        `when`(mugVariantRepository.findByIdWithArticle(999L)).thenReturn(Optional.empty())

        // When/Then
        assertThrows<ResourceNotFoundException> {
            mugVariantService.update(999L, request)
        }
    }
}
