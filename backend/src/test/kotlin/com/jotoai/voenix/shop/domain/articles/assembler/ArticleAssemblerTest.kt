package com.jotoai.voenix.shop.domain.articles.assembler

import com.jotoai.voenix.shop.article.api.dto.MugArticleVariantDto
import com.jotoai.voenix.shop.article.api.dto.ShirtArticleVariantDto
import com.jotoai.voenix.shop.article.api.enums.ArticleType
import com.jotoai.voenix.shop.article.internal.categories.entity.ArticleCategory
import com.jotoai.voenix.shop.article.internal.categories.entity.ArticleSubCategory
import com.jotoai.voenix.shop.article.internal.entity.Article
import com.jotoai.voenix.shop.article.internal.entity.MugArticleVariant
import com.jotoai.voenix.shop.article.internal.entity.ShirtArticleVariant
import com.jotoai.voenix.shop.supplier.api.SupplierQueryService
import com.jotoai.voenix.shop.supplier.api.dto.SupplierDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import java.time.OffsetDateTime

class ArticleAssemblerTest {
    private lateinit var mugArticleVariantAssembler: MugArticleVariantAssembler
    private lateinit var shirtArticleVariantAssembler: ShirtArticleVariantAssembler
    private lateinit var supplierQueryService: SupplierQueryService
    private lateinit var assembler: ArticleAssembler

    private lateinit var testCategory: ArticleCategory
    private lateinit var testSubcategory: ArticleSubCategory
    // Test variants will be created in individual test methods

    @BeforeEach
    fun setUp() {
        mugArticleVariantAssembler = mock(MugArticleVariantAssembler::class.java)
        shirtArticleVariantAssembler = mock(ShirtArticleVariantAssembler::class.java)
        supplierQueryService = mock(SupplierQueryService::class.java)
        assembler = ArticleAssembler(mugArticleVariantAssembler, shirtArticleVariantAssembler, supplierQueryService)

        // Setup test data
        testCategory =
            ArticleCategory(
                id = 1L,
                name = "Test Category",
                description = "Test category description",
            )

        testSubcategory =
            ArticleSubCategory(
                id = 2L,
                articleCategory = testCategory,
                name = "Test Subcategory",
                description = "Test subcategory description",
            )

        // Test variants will be created in individual tests
    }

    @Test
    fun `toDto should convert MUG article with all fields properly mapped`() {
        // Given
        val createdAt = OffsetDateTime.now().minusDays(1)
        val updatedAt = OffsetDateTime.now()

        val entity =
            Article(
                id = 1L,
                name = "Test Mug Article",
                descriptionShort = "Short description",
                descriptionLong = "Long description",
                active = true,
                articleType = ArticleType.MUG,
                category = testCategory,
                subcategory = testSubcategory,
                supplierId = 1L,
                supplierArticleName = "Supplier Mug Name",
                supplierArticleNumber = "SMN-001",
                createdAt = createdAt,
                updatedAt = updatedAt,
            )

        val testMugVariant =
            MugArticleVariant(
                id = 1L,
                article = entity,
                insideColorCode = "#ffffff",
                outsideColorCode = "#000000",
                name = "Black & White Mug",
                isDefault = true,
            )
        entity.mugVariants.add(testMugVariant)

        val expectedMugVariantDto =
            MugArticleVariantDto(
                id = 1L,
                articleId = 1L,
                insideColorCode = "#ffffff",
                outsideColorCode = "#000000",
                name = "Black & White Mug",
                exampleImageUrl = null,
                articleVariantNumber = null,
                isDefault = true,
                createdAt = null,
                updatedAt = null,
            )

        `when`(mugArticleVariantAssembler.toDto(testMugVariant))
            .thenReturn(expectedMugVariantDto)
        `when`(supplierQueryService.getSupplierById(1L))
            .thenReturn(
                SupplierDto(
                    id = 1L,
                    name = "Test Supplier",
                    title = null,
                    firstName = null,
                    lastName = null,
                    street = null,
                    houseNumber = null,
                    city = null,
                    postalCode = null,
                    countryId = null,
                    countryName = null,
                    phoneNumber1 = null,
                    phoneNumber2 = null,
                    phoneNumber3 = null,
                    email = null,
                    website = null,
                    createdAt = null,
                    updatedAt = null,
                ),
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(1L, result.id)
        assertEquals("Test Mug Article", result.name)
        assertEquals("Short description", result.descriptionShort)
        assertEquals("Long description", result.descriptionLong)
        assertEquals(true, result.active)
        assertEquals(ArticleType.MUG, result.articleType)
        assertEquals(1L, result.categoryId)
        assertEquals("Test Category", result.categoryName)
        assertEquals(2L, result.subcategoryId)
        assertEquals("Test Subcategory", result.subcategoryName)
        assertEquals(1L, result.supplierId)
        assertEquals("Test Supplier", result.supplierName)
        assertEquals("Supplier Mug Name", result.supplierArticleName)
        assertEquals("SMN-001", result.supplierArticleNumber)
        assertNotNull(result.mugVariants)
        assertEquals(1, result.mugVariants!!.size)
        assertEquals(expectedMugVariantDto, result.mugVariants!![0])
        assertNull(result.shirtVariants)
        assertEquals(createdAt, result.createdAt)
        assertEquals(updatedAt, result.updatedAt)

        verify(mugArticleVariantAssembler).toDto(testMugVariant)
        verifyNoInteractions(shirtArticleVariantAssembler)
    }

    @Test
    fun `toDto should convert SHIRT article with all fields properly mapped`() {
        // Given
        val entity =
            Article(
                id = 2L,
                name = "Test Shirt Article",
                descriptionShort = "Shirt description short",
                descriptionLong = "Shirt description long",
                active = true,
                articleType = ArticleType.SHIRT,
                category = testCategory,
                subcategory = testSubcategory,
                supplierId = 1L,
                supplierArticleName = "Supplier Shirt Name",
                supplierArticleNumber = "SSN-002",
            )

        val testShirtVariant =
            ShirtArticleVariant(
                id = 1L,
                article = entity,
                color = "Red",
                size = "M",
            )
        entity.shirtVariants.add(testShirtVariant)

        val expectedShirtVariantDto =
            ShirtArticleVariantDto(
                id = 1L,
                articleId = 2L,
                color = "Red",
                size = "M",
                exampleImageUrl = null,
                createdAt = null,
                updatedAt = null,
            )

        `when`(shirtArticleVariantAssembler.toDto(testShirtVariant))
            .thenReturn(expectedShirtVariantDto)
        `when`(supplierQueryService.getSupplierById(1L))
            .thenReturn(
                SupplierDto(
                    id = 1L,
                    name = "Test Supplier",
                    title = null,
                    firstName = null,
                    lastName = null,
                    street = null,
                    houseNumber = null,
                    city = null,
                    postalCode = null,
                    countryId = null,
                    countryName = null,
                    phoneNumber1 = null,
                    phoneNumber2 = null,
                    phoneNumber3 = null,
                    email = null,
                    website = null,
                    createdAt = null,
                    updatedAt = null,
                ),
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(2L, result.id)
        assertEquals("Test Shirt Article", result.name)
        assertEquals(ArticleType.SHIRT, result.articleType)
        assertNull(result.mugVariants)
        assertNotNull(result.shirtVariants)
        assertEquals(1, result.shirtVariants!!.size)
        assertEquals(expectedShirtVariantDto, result.shirtVariants!![0])

        verify(shirtArticleVariantAssembler).toDto(testShirtVariant)
        verifyNoInteractions(mugArticleVariantAssembler)
    }

    @Test
    fun `toDto should handle other article types with null variants`() {
        // Given - Assuming there might be other article types in the future
        val entity =
            Article(
                id = 3L,
                name = "Future Article Type",
                descriptionShort = "Future description",
                descriptionLong = "Future long description",
                active = true,
                articleType = ArticleType.MUG, // Using MUG but will empty variants to simulate other type
                category = testCategory,
            )
        // Deliberately not adding any variants to simulate different article type behavior

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(3L, result.id)
        assertEquals("Future Article Type", result.name)
        assertEquals(ArticleType.MUG, result.articleType)
        assertNotNull(result.mugVariants) // Will be empty list for MUG type
        assertEquals(0, result.mugVariants!!.size)
        assertNull(result.shirtVariants) // Will be null for non-SHIRT type

        verifyNoInteractions(mugArticleVariantAssembler)
        verifyNoInteractions(shirtArticleVariantAssembler)
    }

    @Test
    fun `toDto should handle article without subcategory`() {
        // Given
        val entity =
            Article(
                id = 4L,
                name = "Article Without Subcategory",
                descriptionShort = "Short desc",
                descriptionLong = "Long desc",
                active = true,
                articleType = ArticleType.MUG,
                category = testCategory,
                subcategory = null,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(4L, result.id)
        assertEquals("Article Without Subcategory", result.name)
        assertEquals(1L, result.categoryId)
        assertEquals("Test Category", result.categoryName)
        assertNull(result.subcategoryId)
        assertNull(result.subcategoryName)
    }

    @Test
    fun `toDto should handle article without supplier`() {
        // Given
        val entity =
            Article(
                id = 5L,
                name = "Article Without Supplier",
                descriptionShort = "Short desc",
                descriptionLong = "Long desc",
                active = true,
                articleType = ArticleType.MUG,
                category = testCategory,
                supplierId = null,
                supplierArticleName = null,
                supplierArticleNumber = null,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(5L, result.id)
        assertEquals("Article Without Supplier", result.name)
        assertNull(result.supplierId)
        assertNull(result.supplierName)
        assertNull(result.supplierArticleName)
        assertNull(result.supplierArticleNumber)
    }

    @Test
    fun `toDto should handle null timestamps correctly`() {
        // Given
        val entity =
            Article(
                id = 6L,
                name = "Timestamp Test Article",
                descriptionShort = "Short desc",
                descriptionLong = "Long desc",
                active = true,
                articleType = ArticleType.MUG,
                category = testCategory,
                createdAt = null,
                updatedAt = null,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(6L, result.id)
        assertEquals("Timestamp Test Article", result.name)
        assertNull(result.createdAt)
        assertNull(result.updatedAt)
    }

    @Test
    fun `toDto should throw IllegalArgumentException when entity id is null`() {
        // Given
        val entity =
            Article(
                id = null,
                name = "Invalid Article",
                descriptionShort = "Short",
                descriptionLong = "Long",
                active = true,
                articleType = ArticleType.MUG,
                category = testCategory,
            )

        // When/Then
        val exception =
            assertThrows<IllegalArgumentException> {
                assembler.toDto(entity)
            }

        assertEquals("Article ID cannot be null when converting to DTO", exception.message)
    }

    @Test
    fun `toDto should handle category with valid ID correctly`() {
        // Given
        val validCategory =
            ArticleCategory(
                id = 7L,
                name = "Valid Category",
                description = "Category with valid ID",
            )

        val entity =
            Article(
                id = 7L,
                name = "Article with Valid Category",
                descriptionShort = "Short",
                descriptionLong = "Long",
                active = true,
                articleType = ArticleType.MUG,
                category = validCategory,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(7L, result.id)
        assertEquals(7L, result.categoryId)
        assertEquals("Valid Category", result.categoryName)
    }

    @Test
    fun `toDto should handle MUG article with multiple variants`() {
        // Given
        val entity =
            Article(
                id = 8L,
                name = "Multi-Variant Mug",
                descriptionShort = "Short desc",
                descriptionLong = "Long desc",
                active = true,
                articleType = ArticleType.MUG,
                category = testCategory,
            )

        val mugVariant1 =
            MugArticleVariant(
                id = 1L,
                article = entity,
                insideColorCode = "#ffffff",
                outsideColorCode = "#000000",
                name = "Black & White",
                isDefault = true,
            )

        val mugVariant2 =
            MugArticleVariant(
                id = 2L,
                article = entity,
                insideColorCode = "#ff0000",
                outsideColorCode = "#ffffff",
                name = "Red & White",
                isDefault = false,
            )

        entity.mugVariants.addAll(listOf(mugVariant1, mugVariant2))

        val expectedVariantDto1 =
            MugArticleVariantDto(
                id = 1L,
                articleId = 8L,
                insideColorCode = "#ffffff",
                outsideColorCode = "#000000",
                name = "Black & White",
                exampleImageUrl = null,
                articleVariantNumber = null,
                isDefault = true,
                createdAt = null,
                updatedAt = null,
            )

        val expectedVariantDto2 =
            MugArticleVariantDto(
                id = 2L,
                articleId = 8L,
                insideColorCode = "#ff0000",
                outsideColorCode = "#ffffff",
                name = "Red & White",
                exampleImageUrl = null,
                articleVariantNumber = null,
                isDefault = false,
                createdAt = null,
                updatedAt = null,
            )

        `when`(mugArticleVariantAssembler.toDto(mugVariant1))
            .thenReturn(expectedVariantDto1)
        `when`(mugArticleVariantAssembler.toDto(mugVariant2))
            .thenReturn(expectedVariantDto2)

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(8L, result.id)
        assertEquals(ArticleType.MUG, result.articleType)
        assertNotNull(result.mugVariants)
        assertEquals(2, result.mugVariants!!.size)
        assertEquals(expectedVariantDto1, result.mugVariants!![0])
        assertEquals(expectedVariantDto2, result.mugVariants!![1])
        assertNull(result.shirtVariants)

        verify(mugArticleVariantAssembler).toDto(mugVariant1)
        verify(mugArticleVariantAssembler).toDto(mugVariant2)
        verifyNoInteractions(shirtArticleVariantAssembler)
    }

    @Test
    fun `toDto should handle SHIRT article with multiple variants`() {
        // Given
        val entity =
            Article(
                id = 9L,
                name = "Multi-Variant Shirt",
                descriptionShort = "Short desc",
                descriptionLong = "Long desc",
                active = true,
                articleType = ArticleType.SHIRT,
                category = testCategory,
            )

        val shirtVariant1 =
            ShirtArticleVariant(
                id = 1L,
                article = entity,
                color = "Red",
                size = "M",
            )

        val shirtVariant2 =
            ShirtArticleVariant(
                id = 2L,
                article = entity,
                color = "Blue",
                size = "L",
            )

        entity.shirtVariants.addAll(listOf(shirtVariant1, shirtVariant2))

        val expectedVariantDto1 =
            ShirtArticleVariantDto(
                id = 1L,
                articleId = 9L,
                color = "Red",
                size = "M",
                exampleImageUrl = null,
                createdAt = null,
                updatedAt = null,
            )

        val expectedVariantDto2 =
            ShirtArticleVariantDto(
                id = 2L,
                articleId = 9L,
                color = "Blue",
                size = "L",
                exampleImageUrl = null,
                createdAt = null,
                updatedAt = null,
            )

        `when`(shirtArticleVariantAssembler.toDto(shirtVariant1))
            .thenReturn(expectedVariantDto1)
        `when`(shirtArticleVariantAssembler.toDto(shirtVariant2))
            .thenReturn(expectedVariantDto2)

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(9L, result.id)
        assertEquals(ArticleType.SHIRT, result.articleType)
        assertNull(result.mugVariants)
        assertNotNull(result.shirtVariants)
        assertEquals(2, result.shirtVariants!!.size)
        assertEquals(expectedVariantDto1, result.shirtVariants!![0])
        assertEquals(expectedVariantDto2, result.shirtVariants!![1])

        verify(shirtArticleVariantAssembler).toDto(shirtVariant1)
        verify(shirtArticleVariantAssembler).toDto(shirtVariant2)
        verifyNoInteractions(mugArticleVariantAssembler)
    }

    @Test
    fun `toDto should handle long text values correctly`() {
        // Given
        val longName = "A" + "very ".repeat(30) + "long article name"
        val longShortDesc = "A" + "very ".repeat(50) + "long short description"
        val longLongDesc = "A" + "very ".repeat(100) + "long long description"

        val entity =
            Article(
                id = 10L,
                name = longName,
                descriptionShort = longShortDesc,
                descriptionLong = longLongDesc,
                active = true,
                articleType = ArticleType.MUG,
                category = testCategory,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(longName, result.name)
        assertEquals(longShortDesc, result.descriptionShort)
        assertEquals(longLongDesc, result.descriptionLong)
    }

    @Test
    fun `toDto should handle complex category and supplier scenarios`() {
        // Given
        val complexCategory =
            ArticleCategory(
                id = 999L,
                name = "Complex & Special Characters",
                description = "Category with special chars",
            )

        val complexSubcategory =
            ArticleSubCategory(
                id = 888L,
                articleCategory = complexCategory,
                name = "Complex Sub & Category",
                description = "Subcategory with special chars",
            )

        val entity =
            Article(
                id = 11L,
                name = "Complex Article",
                descriptionShort = "Complex short",
                descriptionLong = "Complex long",
                active = true,
                articleType = ArticleType.MUG,
                category = complexCategory,
                subcategory = complexSubcategory,
                supplierId = 777L,
                supplierArticleName = "Complex & Supplier Article",
                supplierArticleNumber = "CSA-999",
            )

        // Mock the supplier query
        `when`(supplierQueryService.getSupplierById(777L))
            .thenReturn(
                SupplierDto(
                    id = 777L,
                    name = "Complex & Special Supplier",
                    title = null,
                    firstName = null,
                    lastName = null,
                    street = null,
                    houseNumber = null,
                    city = null,
                    postalCode = null,
                    countryId = null,
                    countryName = null,
                    phoneNumber1 = null,
                    phoneNumber2 = null,
                    phoneNumber3 = null,
                    email = null,
                    website = null,
                    createdAt = null,
                    updatedAt = null,
                ),
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(11L, result.id)
        assertEquals(999L, result.categoryId)
        assertEquals("Complex & Special Characters", result.categoryName)
        assertEquals(888L, result.subcategoryId)
        assertEquals("Complex Sub & Category", result.subcategoryName)
        assertEquals(777L, result.supplierId)
        assertEquals("Complex & Special Supplier", result.supplierName)
        assertEquals("Complex & Supplier Article", result.supplierArticleName)
        assertEquals("CSA-999", result.supplierArticleNumber)
    }

    @Test
    fun `toDto should handle inactive article correctly`() {
        // Given
        val entity =
            Article(
                id = 12L,
                name = "Inactive Article",
                descriptionShort = "Short desc",
                descriptionLong = "Long desc",
                active = false,
                articleType = ArticleType.MUG,
                category = testCategory,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(12L, result.id)
        assertEquals("Inactive Article", result.name)
        assertEquals(false, result.active)
    }

    @Test
    fun `toDto should handle minimal required fields only`() {
        // Given
        val entity =
            Article(
                id = 13L,
                name = "Minimal Article",
                descriptionShort = "Minimal short",
                descriptionLong = "Minimal long",
                active = true,
                articleType = ArticleType.MUG,
                category = testCategory,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(13L, result.id)
        assertEquals("Minimal Article", result.name)
        assertEquals("Minimal short", result.descriptionShort)
        assertEquals("Minimal long", result.descriptionLong)
        assertEquals(true, result.active)
        assertEquals(ArticleType.MUG, result.articleType)
        assertEquals(1L, result.categoryId)
        assertEquals("Test Category", result.categoryName)
        assertNull(result.subcategoryId)
        assertNull(result.subcategoryName)
        assertNull(result.supplierId)
        assertNull(result.supplierName)
        assertNull(result.supplierArticleName)
        assertNull(result.supplierArticleNumber)
        assertNotNull(result.mugVariants)
        assertEquals(0, result.mugVariants!!.size)
        assertNull(result.shirtVariants)
        assertNull(result.createdAt)
        assertNull(result.updatedAt)
    }
}
