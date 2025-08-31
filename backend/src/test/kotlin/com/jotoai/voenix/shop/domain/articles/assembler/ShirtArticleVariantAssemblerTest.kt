package com.jotoai.voenix.shop.domain.articles.assembler

import com.jotoai.voenix.shop.article.api.enums.ArticleType
import com.jotoai.voenix.shop.article.internal.assembler.ShirtArticleVariantAssembler
import com.jotoai.voenix.shop.article.internal.categories.entity.ArticleCategory
import com.jotoai.voenix.shop.article.internal.entity.Article
import com.jotoai.voenix.shop.article.internal.entity.ShirtArticleVariant
import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.image.ImageType
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime

class ShirtArticleVariantAssemblerTest {
    private lateinit var imageService: ImageService
    private lateinit var assembler: ShirtArticleVariantAssembler

    private lateinit var testArticle: Article
    private lateinit var testCategory: ArticleCategory

    @BeforeEach
    fun setUp() {
        imageService = mockk()
        assembler = ShirtArticleVariantAssembler(imageService)

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
                articleType = ArticleType.SHIRT,
                name = "Test Shirt",
                descriptionShort = "Test Description Short",
                descriptionLong = "Test Description Long",
                active = true,
                category = testCategory,
                supplierArticleName = "Test Supplier Shirt",
                supplierArticleNumber = "TSS-001",
            )
    }

    @Test
    fun `toDto should convert entity to DTO with all fields properly mapped`() {
        // Given
        val createdAt = OffsetDateTime.now().minusDays(1)
        val updatedAt = OffsetDateTime.now()
        val exampleImageFilename = "red-shirt-large.jpg"
        val expectedImageUrl = "https://example.com/images/shirt-variants/red-shirt-large.jpg"

        val entity =
            ShirtArticleVariant(
                id = 1L,
                article = testArticle,
                color = "Red",
                size = "L",
                exampleImageFilename = exampleImageFilename,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )

        every {
            imageService.getUrl(
                exampleImageFilename,
                ImageType.SHIRT_VARIANT_EXAMPLE,
            )
        } returns expectedImageUrl

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(1L, result.id)
        assertEquals(1L, result.articleId)
        assertEquals("Red", result.color)
        assertEquals("L", result.size)
        assertEquals(expectedImageUrl, result.exampleImageUrl)
        assertEquals(createdAt, result.createdAt)
        assertEquals(updatedAt, result.updatedAt)

        verify { imageService.getUrl(exampleImageFilename, ImageType.SHIRT_VARIANT_EXAMPLE) }
    }

    @Test
    fun `toDto should handle null exampleImageFilename correctly`() {
        // Given
        val entity =
            ShirtArticleVariant(
                id = 2L,
                article = testArticle,
                color = "Blue",
                size = "M",
                exampleImageFilename = null,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(2L, result.id)
        assertEquals(1L, result.articleId)
        assertEquals("Blue", result.color)
        assertEquals("M", result.size)
        assertNull(result.exampleImageUrl)

        // Verify ImageService has no interactions when filename is null
        verify(exactly = 0) { imageService.getUrl(any(), any()) }
    }

    @Test
    fun `toDto should handle null timestamps correctly`() {
        // Given
        val entity =
            ShirtArticleVariant(
                id = 3L,
                article = testArticle,
                color = "Green",
                size = "S",
                exampleImageFilename = "green-shirt-small.png",
                createdAt = null,
                updatedAt = null,
            )

        val expectedImageUrl = "https://example.com/images/shirt-variants/green-shirt-small.png"
        every {
            imageService.getUrl(
                "green-shirt-small.png",
                ImageType.SHIRT_VARIANT_EXAMPLE,
            )
        } returns expectedImageUrl

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(3L, result.id)
        assertEquals("Green", result.color)
        assertEquals("S", result.size)
        assertEquals(expectedImageUrl, result.exampleImageUrl)
        assertNull(result.createdAt)
        assertNull(result.updatedAt)
    }

    @Test
    fun `toDto should throw IllegalArgumentException when entity id is null`() {
        // Given
        val entity =
            ShirtArticleVariant(
                id = null,
                article = testArticle,
                color = "Black",
                size = "XL",
            )

        // When/Then
        val exception =
            assertThrows<IllegalArgumentException> {
                assembler.toDto(entity)
            }

        assertEquals("ShirtArticleVariant ID cannot be null when converting to DTO", exception.message)
    }

    @Test
    fun `toDto should call StoragePathService with correct parameters`() {
        // Given
        val filename = "custom-shirt-variant.webp"
        val entity =
            ShirtArticleVariant(
                id = 4L,
                article = testArticle,
                color = "Purple",
                size = "XS",
                exampleImageFilename = filename,
            )

        val expectedUrl = "https://storage.example.com/shirts/custom-shirt-variant.webp"
        every {
            imageService.getUrl(
                filename,
                ImageType.SHIRT_VARIANT_EXAMPLE,
            )
        } returns expectedUrl

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(expectedUrl, result.exampleImageUrl)
        verify { imageService.getUrl(filename, ImageType.SHIRT_VARIANT_EXAMPLE) }
    }

    @Test
    fun `toDto should handle different color values correctly`() {
        // Given
        val entity =
            ShirtArticleVariant(
                id = 5L,
                article = testArticle,
                color = "Navy Blue",
                size = "XXL",
                exampleImageFilename = null,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(5L, result.id)
        assertEquals("Navy Blue", result.color)
        assertEquals("XXL", result.size)
        assertNull(result.exampleImageUrl)
    }

    @Test
    fun `toDto should handle different size values correctly`() {
        // Given
        val entity =
            ShirtArticleVariant(
                id = 6L,
                article = testArticle,
                color = "White",
                size = "3XL",
                exampleImageFilename = "white-shirt-3xl.jpg",
            )

        val expectedUrl = "https://cdn.example.com/shirt-variants/white-shirt-3xl.jpg"
        every {
            imageService.getUrl(
                "white-shirt-3xl.jpg",
                ImageType.SHIRT_VARIANT_EXAMPLE,
            )
        } returns expectedUrl

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(6L, result.id)
        assertEquals("White", result.color)
        assertEquals("3XL", result.size)
        assertEquals(expectedUrl, result.exampleImageUrl)
    }

    @Test
    fun `toDto should handle minimal entity with only required fields`() {
        // Given
        val entity =
            ShirtArticleVariant(
                id = 7L,
                article = testArticle,
                color = "Gray",
                size = "M",
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(7L, result.id)
        assertEquals(1L, result.articleId)
        assertEquals("Gray", result.color)
        assertEquals("M", result.size)
        assertNull(result.exampleImageUrl)
        assertNull(result.createdAt)
        assertNull(result.updatedAt)
    }

    @Test
    fun `toDto should handle special characters in color names`() {
        // Given
        val entity =
            ShirtArticleVariant(
                id = 8L,
                article = testArticle,
                color = "Mint & Lime",
                size = "L",
                exampleImageFilename = "mint-lime-shirt.png",
            )

        val expectedUrl = "https://example.com/images/mint-lime-shirt.png"
        every {
            imageService.getUrl(
                "mint-lime-shirt.png",
                ImageType.SHIRT_VARIANT_EXAMPLE,
            )
        } returns expectedUrl

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals("Mint & Lime", result.color)
        assertEquals(expectedUrl, result.exampleImageUrl)
    }
}
