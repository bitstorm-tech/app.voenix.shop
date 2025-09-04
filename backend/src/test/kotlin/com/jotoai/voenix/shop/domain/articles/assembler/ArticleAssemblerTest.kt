package com.jotoai.voenix.shop.domain.articles.assembler

import com.jotoai.voenix.shop.article.ArticleDto
import com.jotoai.voenix.shop.article.ArticleType
import com.jotoai.voenix.shop.article.MugArticleVariantDto
import com.jotoai.voenix.shop.article.internal.assembler.ArticleAssembler
import com.jotoai.voenix.shop.article.internal.assembler.MugArticleVariantAssembler
import com.jotoai.voenix.shop.article.internal.assembler.ShirtArticleVariantAssembler
import com.jotoai.voenix.shop.article.ShirtArticleVariantDto
import com.jotoai.voenix.shop.article.internal.entity.Article
import com.jotoai.voenix.shop.article.internal.entity.ArticleCategory
import com.jotoai.voenix.shop.article.internal.entity.ArticleSubCategory
import com.jotoai.voenix.shop.article.internal.entity.MugArticleVariant
import com.jotoai.voenix.shop.article.internal.entity.ShirtArticleVariant
import com.jotoai.voenix.shop.supplier.SupplierDto
import com.jotoai.voenix.shop.supplier.SupplierService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime

@Suppress("LongMethod", "LargeClass")
class ArticleAssemblerTest {
    private lateinit var mugArticleVariantAssembler: MugArticleVariantAssembler
    private lateinit var shirtArticleVariantAssembler: ShirtArticleVariantAssembler
    private lateinit var supplierService: SupplierService
    private lateinit var assembler: ArticleAssembler

    private lateinit var testCategory: ArticleCategory
    private lateinit var testSubcategory: ArticleSubCategory
    // Test variants will be created in individual test methods

    @BeforeEach
    fun setUp() {
        mugArticleVariantAssembler = mockk()
        shirtArticleVariantAssembler = mockk()
        supplierService = mockk()
        assembler = ArticleAssembler(mugArticleVariantAssembler, shirtArticleVariantAssembler, supplierService)

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
        val entity = createTestMugArticle(createdAt, updatedAt)
        val testMugVariant = createTestMugVariant(entity)
        entity.mugVariants.add(testMugVariant)

        val expectedMugVariantDto = createExpectedMugVariantDto()
        setupMugArticleTestMocks(testMugVariant, expectedMugVariantDto)

        // When
        val result = assembler.toDto(entity)

        // Then
        verifyMugArticleFields(result, createdAt, updatedAt, expectedMugVariantDto)
        verifyMugArticleInteractions(testMugVariant)
    }

    @Test
    fun `toDto should convert SHIRT article with all fields properly mapped`() {
        // Given
        val entity = createTestShirtArticle()
        val testShirtVariant = createTestShirtVariant(entity)
        entity.shirtVariants.add(testShirtVariant)

        val expectedShirtVariantDto = createExpectedShirtVariantDto()
        setupShirtArticleTestMocks(testShirtVariant, expectedShirtVariantDto)

        // When
        val result = assembler.toDto(entity)

        // Then
        verifyShirtArticleFields(result, expectedShirtVariantDto)
        verifyShirtArticleInteractions(testShirtVariant)
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

        verify(exactly = 0) { mugArticleVariantAssembler.toDto(any()) }
        verify(exactly = 0) { shirtArticleVariantAssembler.toDto(any()) }
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
        val entity = createMultiVariantMugArticle()
        val (mugVariant1, mugVariant2) = createMultipleMugVariants(entity)
        entity.mugVariants.addAll(listOf(mugVariant1, mugVariant2))

        val (expectedDto1, expectedDto2) = createExpectedMultipleMugVariantDtos()
        setupMultipleMugVariantMocks(mugVariant1, mugVariant2, expectedDto1, expectedDto2)

        // When
        val result = assembler.toDto(entity)

        // Then
        verifyMultipleMugVariantsFields(result, expectedDto1, expectedDto2)
        verifyMultipleMugVariantsInteractions(mugVariant1, mugVariant2)
    }

    @Test
    fun `toDto should handle SHIRT article with multiple variants`() {
        // Given
        val entity = createMultiVariantShirtArticle()
        val (shirtVariant1, shirtVariant2) = createMultipleShirtVariants(entity)
        entity.shirtVariants.addAll(listOf(shirtVariant1, shirtVariant2))

        val (expectedDto1, expectedDto2) = createExpectedMultipleShirtVariantDtos()
        setupMultipleShirtVariantMocks(shirtVariant1, shirtVariant2, expectedDto1, expectedDto2)

        // When
        val result = assembler.toDto(entity)

        // Then
        verifyMultipleShirtVariantsFields(result, expectedDto1, expectedDto2)
        verifyMultipleShirtVariantsInteractions(shirtVariant1, shirtVariant2)
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
        val (complexCategory, complexSubcategory) = createComplexCategoryAndSubcategory()
        val entity = createComplexArticle(complexCategory, complexSubcategory)
        setupComplexSupplierMock()

        // When
        val result = assembler.toDto(entity)

        // Then
        verifyComplexCategoryAndSupplierFields(result)
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

    private fun createTestMugArticle(
        createdAt: OffsetDateTime,
        updatedAt: OffsetDateTime,
    ) = Article(
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

    private fun createTestMugVariant(entity: Article) =
        MugArticleVariant(
            id = 1L,
            article = entity,
            insideColorCode = "#ffffff",
            outsideColorCode = "#000000",
            name = "Black & White Mug",
            isDefault = true,
        )

    private fun createExpectedMugVariantDto() =
        MugArticleVariantDto(
            id = 1L,
            articleId = 1L,
            insideColorCode = "#ffffff",
            outsideColorCode = "#000000",
            name = "Black & White Mug",
            exampleImageUrl = null,
            articleVariantNumber = null,
            isDefault = true,
            active = true,
            createdAt = null,
            updatedAt = null,
        )

    private fun setupMugArticleTestMocks(
        testMugVariant: MugArticleVariant,
        expectedDto: MugArticleVariantDto,
    ) {
        every { mugArticleVariantAssembler.toDto(testMugVariant) } returns expectedDto
        every { supplierService.getSupplierById(1L) } returns createTestSupplierDto()
    }

    private fun createTestSupplierDto() =
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
        )

    private fun verifyMugArticleFields(
        result: ArticleDto,
        createdAt: OffsetDateTime,
        updatedAt: OffsetDateTime,
        expectedVariant: MugArticleVariantDto,
    ) {
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
        assertEquals(expectedVariant, result.mugVariants!![0])
        assertNull(result.shirtVariants)
        assertEquals(createdAt, result.createdAt)
        assertEquals(updatedAt, result.updatedAt)
    }

    private fun verifyMugArticleInteractions(testMugVariant: MugArticleVariant) {
        verify { mugArticleVariantAssembler.toDto(testMugVariant) }
        verify { supplierService.getSupplierById(1L) }
        verify(exactly = 0) { shirtArticleVariantAssembler.toDto(any()) }
    }

    // Helper methods for SHIRT article test
    private fun createTestShirtArticle() =
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

    private fun createTestShirtVariant(entity: Article) =
        ShirtArticleVariant(
            id = 1L,
            article = entity,
            color = "Red",
            size = "M",
        )

    private fun createExpectedShirtVariantDto() =
        ShirtArticleVariantDto(
            id = 1L,
            articleId = 2L,
            color = "Red",
            size = "M",
            exampleImageUrl = null,
            createdAt = null,
            updatedAt = null,
        )

    private fun setupShirtArticleTestMocks(
        testShirtVariant: ShirtArticleVariant,
        expectedDto: ShirtArticleVariantDto,
    ) {
        every { shirtArticleVariantAssembler.toDto(testShirtVariant) } returns expectedDto
        every { supplierService.getSupplierById(1L) } returns createTestSupplierDto()
    }

    private fun verifyShirtArticleFields(
        result: ArticleDto,
        expectedVariant: ShirtArticleVariantDto,
    ) {
        assertEquals(2L, result.id)
        assertEquals("Test Shirt Article", result.name)
        assertEquals(ArticleType.SHIRT, result.articleType)
        assertNull(result.mugVariants)
        assertNotNull(result.shirtVariants)
        assertEquals(1, result.shirtVariants!!.size)
        assertEquals(expectedVariant, result.shirtVariants!![0])
    }

    private fun verifyShirtArticleInteractions(testShirtVariant: ShirtArticleVariant) {
        verify { shirtArticleVariantAssembler.toDto(testShirtVariant) }
        verify(exactly = 0) { mugArticleVariantAssembler.toDto(any()) }
    }

    // Helper methods for multiple MUG variants test
    private fun createMultiVariantMugArticle() =
        Article(
            id = 8L,
            name = "Multi-Variant Mug",
            descriptionShort = "Short desc",
            descriptionLong = "Long desc",
            active = true,
            articleType = ArticleType.MUG,
            category = testCategory,
        )

    private fun createMultipleMugVariants(entity: Article): Pair<MugArticleVariant, MugArticleVariant> {
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

        return Pair(mugVariant1, mugVariant2)
    }

    private fun createExpectedMultipleMugVariantDtos(): Pair<MugArticleVariantDto, MugArticleVariantDto> {
        val expectedDto1 =
            MugArticleVariantDto(
                id = 1L,
                articleId = 8L,
                insideColorCode = "#ffffff",
                outsideColorCode = "#000000",
                name = "Black & White",
                exampleImageUrl = null,
                articleVariantNumber = null,
                isDefault = true,
                active = true,
                createdAt = null,
                updatedAt = null,
            )

        val expectedDto2 =
            MugArticleVariantDto(
                id = 2L,
                articleId = 8L,
                insideColorCode = "#ff0000",
                outsideColorCode = "#ffffff",
                name = "Red & White",
                exampleImageUrl = null,
                articleVariantNumber = null,
                isDefault = false,
                active = true,
                createdAt = null,
                updatedAt = null,
            )

        return Pair(expectedDto1, expectedDto2)
    }

    private fun setupMultipleMugVariantMocks(
        variant1: MugArticleVariant,
        variant2: MugArticleVariant,
        dto1: MugArticleVariantDto,
        dto2: MugArticleVariantDto,
    ) {
        every { mugArticleVariantAssembler.toDto(variant1) } returns dto1
        every { mugArticleVariantAssembler.toDto(variant2) } returns dto2
    }

    private fun verifyMultipleMugVariantsFields(
        result: ArticleDto,
        expectedDto1: MugArticleVariantDto,
        expectedDto2: MugArticleVariantDto,
    ) {
        assertEquals(8L, result.id)
        assertEquals(ArticleType.MUG, result.articleType)
        assertNotNull(result.mugVariants)
        assertEquals(2, result.mugVariants!!.size)
        assertEquals(expectedDto1, result.mugVariants!![0])
        assertEquals(expectedDto2, result.mugVariants!![1])
        assertNull(result.shirtVariants)
    }

    private fun verifyMultipleMugVariantsInteractions(
        variant1: MugArticleVariant,
        variant2: MugArticleVariant,
    ) {
        verify { mugArticleVariantAssembler.toDto(variant1) }
        verify { mugArticleVariantAssembler.toDto(variant2) }
        verify(exactly = 0) { shirtArticleVariantAssembler.toDto(any()) }
    }

    // Helper methods for multiple SHIRT variants test
    private fun createMultiVariantShirtArticle() =
        Article(
            id = 9L,
            name = "Multi-Variant Shirt",
            descriptionShort = "Short desc",
            descriptionLong = "Long desc",
            active = true,
            articleType = ArticleType.SHIRT,
            category = testCategory,
        )

    private fun createMultipleShirtVariants(entity: Article): Pair<ShirtArticleVariant, ShirtArticleVariant> {
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

        return Pair(shirtVariant1, shirtVariant2)
    }

    private fun createExpectedMultipleShirtVariantDtos(): Pair<ShirtArticleVariantDto, ShirtArticleVariantDto> {
        val expectedDto1 =
            ShirtArticleVariantDto(
                id = 1L,
                articleId = 9L,
                color = "Red",
                size = "M",
                exampleImageUrl = null,
                createdAt = null,
                updatedAt = null,
            )

        val expectedDto2 =
            ShirtArticleVariantDto(
                id = 2L,
                articleId = 9L,
                color = "Blue",
                size = "L",
                exampleImageUrl = null,
                createdAt = null,
                updatedAt = null,
            )

        return Pair(expectedDto1, expectedDto2)
    }

    private fun setupMultipleShirtVariantMocks(
        variant1: ShirtArticleVariant,
        variant2: ShirtArticleVariant,
        dto1: ShirtArticleVariantDto,
        dto2: ShirtArticleVariantDto,
    ) {
        every { shirtArticleVariantAssembler.toDto(variant1) } returns dto1
        every { shirtArticleVariantAssembler.toDto(variant2) } returns dto2
    }

    private fun verifyMultipleShirtVariantsFields(
        result: ArticleDto,
        expectedDto1: ShirtArticleVariantDto,
        expectedDto2: ShirtArticleVariantDto,
    ) {
        assertEquals(9L, result.id)
        assertEquals(ArticleType.SHIRT, result.articleType)
        assertNull(result.mugVariants)
        assertNotNull(result.shirtVariants)
        assertEquals(2, result.shirtVariants!!.size)
        assertEquals(expectedDto1, result.shirtVariants!![0])
        assertEquals(expectedDto2, result.shirtVariants!![1])
    }

    private fun verifyMultipleShirtVariantsInteractions(
        variant1: ShirtArticleVariant,
        variant2: ShirtArticleVariant,
    ) {
        verify { shirtArticleVariantAssembler.toDto(variant1) }
        verify { shirtArticleVariantAssembler.toDto(variant2) }
        verify(exactly = 0) { mugArticleVariantAssembler.toDto(any()) }
    }

    // Helper methods for complex category and supplier test
    private fun createComplexCategoryAndSubcategory(): Pair<ArticleCategory, ArticleSubCategory> {
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

        return Pair(complexCategory, complexSubcategory)
    }

    private fun createComplexArticle(
        complexCategory: ArticleCategory,
        complexSubcategory: ArticleSubCategory,
    ) = Article(
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

    private fun setupComplexSupplierMock() {
        every { supplierService.getSupplierById(777L) } returns
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
            )
    }

    private fun verifyComplexCategoryAndSupplierFields(result: ArticleDto) {
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
}
