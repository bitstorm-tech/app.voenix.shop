package com.jotoai.voenix.shop.article.internal.assembler

import com.jotoai.voenix.shop.article.api.enums.ArticleType
import com.jotoai.voenix.shop.article.internal.categories.entity.ArticleCategory
import com.jotoai.voenix.shop.article.internal.entity.Article
import com.jotoai.voenix.shop.article.internal.entity.MugArticleVariant
import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import java.time.OffsetDateTime

class MugArticleVariantAssemblerTest {
    private lateinit var storagePathService: StoragePathService
    private lateinit var assembler: MugArticleVariantAssembler

    private lateinit var testArticle: Article
    private lateinit var testCategory: ArticleCategory

    @BeforeEach
    fun setUp() {
        storagePathService = mock(StoragePathService::class.java)
        assembler = MugArticleVariantAssembler(storagePathService)

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
    fun `toDto should convert entity to DTO with all fields properly mapped`() {
        // Given
        val createdAt = OffsetDateTime.now().minusDays(1)
        val updatedAt = OffsetDateTime.now()
        val exampleImageFilename = "test-image.jpg"
        val expectedImageUrl = "https://example.com/images/mug-variants/test-image.jpg"

        val entity =
            MugArticleVariant(
                id = 1L,
                article = testArticle,
                insideColorCode = "#ffffff",
                outsideColorCode = "#000000",
                name = "Black & White Mug",
                exampleImageFilename = exampleImageFilename,
                articleVariantNumber = "BWM-001",
                isDefault = true,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )

        `when`(storagePathService.getImageUrl(ImageType.MUG_VARIANT_EXAMPLE, exampleImageFilename))
            .thenReturn(expectedImageUrl)

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(1L, result.id)
        assertEquals(1L, result.articleId)
        assertEquals("#ffffff", result.insideColorCode)
        assertEquals("#000000", result.outsideColorCode)
        assertEquals("Black & White Mug", result.name)
        assertEquals(expectedImageUrl, result.exampleImageUrl)
        assertEquals("BWM-001", result.articleVariantNumber)
        assertEquals(true, result.isDefault)
        assertEquals(createdAt, result.createdAt)
        assertEquals(updatedAt, result.updatedAt)

        verify(storagePathService).getImageUrl(ImageType.MUG_VARIANT_EXAMPLE, exampleImageFilename)
    }

    @Test
    fun `toDto should handle null exampleImageFilename correctly`() {
        // Given
        val entity =
            MugArticleVariant(
                id = 2L,
                article = testArticle,
                insideColorCode = "#ff0000",
                outsideColorCode = "#ffffff",
                name = "Red & White Mug",
                exampleImageFilename = null,
                articleVariantNumber = "RWM-002",
                isDefault = false,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(2L, result.id)
        assertEquals(1L, result.articleId)
        assertEquals("#ff0000", result.insideColorCode)
        assertEquals("#ffffff", result.outsideColorCode)
        assertEquals("Red & White Mug", result.name)
        assertNull(result.exampleImageUrl)
        assertEquals("RWM-002", result.articleVariantNumber)
        assertEquals(false, result.isDefault)

        // Verify StoragePathService has no interactions when filename is null
        verifyNoInteractions(storagePathService)
    }

    @Test
    fun `toDto should handle null articleVariantNumber correctly`() {
        // Given
        val entity =
            MugArticleVariant(
                id = 3L,
                article = testArticle,
                insideColorCode = "#00ff00",
                outsideColorCode = "#000000",
                name = "Green & Black Mug",
                exampleImageFilename = "green-mug.jpg",
                articleVariantNumber = null,
                isDefault = false,
            )

        val expectedImageUrl = "https://example.com/images/mug-variants/green-mug.jpg"
        `when`(storagePathService.getImageUrl(ImageType.MUG_VARIANT_EXAMPLE, "green-mug.jpg"))
            .thenReturn(expectedImageUrl)

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(3L, result.id)
        assertEquals("Green & Black Mug", result.name)
        assertEquals(expectedImageUrl, result.exampleImageUrl)
        assertNull(result.articleVariantNumber)
    }

    @Test
    fun `toDto should handle null timestamps correctly`() {
        // Given
        val entity =
            MugArticleVariant(
                id = 4L,
                article = testArticle,
                insideColorCode = "#0000ff",
                outsideColorCode = "#ffffff",
                name = "Blue & White Mug",
                exampleImageFilename = null,
                articleVariantNumber = "BWM-004",
                isDefault = true,
                createdAt = null,
                updatedAt = null,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(4L, result.id)
        assertEquals("Blue & White Mug", result.name)
        assertNull(result.createdAt)
        assertNull(result.updatedAt)
    }

    @Test
    fun `toDto should throw IllegalArgumentException when entity id is null`() {
        // Given
        val entity =
            MugArticleVariant(
                id = null,
                article = testArticle,
                insideColorCode = "#ffffff",
                outsideColorCode = "#000000",
                name = "Invalid Mug",
                isDefault = false,
            )

        // When/Then
        val exception =
            assertThrows<IllegalArgumentException> {
                assembler.toDto(entity)
            }

        assertEquals("MugArticleVariant ID cannot be null when converting to DTO", exception.message)
    }

    @Test
    fun `toDto should call StoragePathService with correct parameters`() {
        // Given
        val filename = "custom-mug-variant.png"
        val entity =
            MugArticleVariant(
                id = 5L,
                article = testArticle,
                insideColorCode = "#ffff00",
                outsideColorCode = "#ff00ff",
                name = "Yellow & Magenta Mug",
                exampleImageFilename = filename,
                isDefault = false,
            )

        val expectedUrl = "https://storage.example.com/mug-variants/custom-mug-variant.png"
        `when`(storagePathService.getImageUrl(ImageType.MUG_VARIANT_EXAMPLE, filename))
            .thenReturn(expectedUrl)

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(expectedUrl, result.exampleImageUrl)
        verify(storagePathService).getImageUrl(ImageType.MUG_VARIANT_EXAMPLE, filename)
    }

    @Test
    fun `toDto should handle minimal entity with only required fields`() {
        // Given
        val entity =
            MugArticleVariant(
                id = 6L,
                article = testArticle,
                name = "Minimal Mug",
                insideColorCode = "#ffffff",
                outsideColorCode = "#000000",
                isDefault = false,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(6L, result.id)
        assertEquals(1L, result.articleId)
        assertEquals("#ffffff", result.insideColorCode)
        assertEquals("#000000", result.outsideColorCode)
        assertEquals("Minimal Mug", result.name)
        assertNull(result.exampleImageUrl)
        assertNull(result.articleVariantNumber)
        assertEquals(false, result.isDefault)
        assertNull(result.createdAt)
        assertNull(result.updatedAt)
    }
}
