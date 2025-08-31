package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.image.ImageType
import com.jotoai.voenix.shop.prompt.api.dto.pub.PublicPromptSlotDto
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.PromptSlotVariantDto
import com.jotoai.voenix.shop.prompt.internal.entity.Prompt
import com.jotoai.voenix.shop.prompt.internal.entity.PromptCategory
import com.jotoai.voenix.shop.prompt.internal.entity.PromptSlotType
import com.jotoai.voenix.shop.prompt.internal.entity.PromptSlotVariant
import com.jotoai.voenix.shop.prompt.internal.entity.PromptSlotVariantMapping
import com.jotoai.voenix.shop.prompt.internal.entity.PromptSlotVariantMappingId
import com.jotoai.voenix.shop.prompt.internal.entity.PromptSubCategory
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

class PromptAssemblerTest {
    private lateinit var promptSlotVariantAssembler: PromptSlotVariantAssembler
    private lateinit var imageService: ImageService
    private lateinit var assembler: PromptAssembler

    private lateinit var testCategory: PromptCategory
    private lateinit var testSubcategory: PromptSubCategory
    private lateinit var testSlotType: PromptSlotType
    private lateinit var testSlotVariant: PromptSlotVariant

    @BeforeEach
    fun setUp() {
        promptSlotVariantAssembler = mockk()
        imageService = mockk()
        assembler = PromptAssembler(promptSlotVariantAssembler, imageService)

        // Setup test data
        testCategory =
            PromptCategory(
                id = 1L,
                name = "Test Category",
            )

        testSubcategory =
            PromptSubCategory(
                id = 2L,
                name = "Test Subcategory",
                description = "Test subcategory description",
                promptCategory = testCategory,
            )

        testSlotType =
            PromptSlotType(
                id = 1L,
                name = "Background",
                position = 1,
            )

        testSlotVariant =
            PromptSlotVariant(
                id = 1L,
                promptSlotTypeId = 1L,
                promptSlotType = testSlotType,
                name = "Nature Background",
                prompt = "A beautiful natural landscape",
                description = "Perfect for nature themes",
            )
    }

    @Test
    fun `toDto should convert entity to DTO with all fields properly mapped`() {
        // Given
        val createdAt = OffsetDateTime.now().minusDays(1)
        val updatedAt = OffsetDateTime.now()
        val exampleImageFilename = "prompt-example.jpg"
        val expectedImageUrl = "https://example.com/images/prompt-examples/prompt-example.jpg"

        val entity = createTestPromptWithAllFields(createdAt, updatedAt, exampleImageFilename)
        val mapping = createTestSlotVariantMapping(entity)
        entity.promptSlotVariantMappings.add(mapping)

        val expectedSlotDto = createExpectedSlotDto()
        setupPromptTestMocks(exampleImageFilename, expectedImageUrl, expectedSlotDto)

        // When
        val result = assembler.toDto(entity)

        // Then
        verifyPromptDtoFields(result, expectedSlotDto, expectedImageUrl, createdAt, updatedAt)
        verifyPromptTestInteractions(exampleImageFilename)
    }

    @Test
    fun `toDto should handle prompt without slots`() {
        // Given
        val entity =
            Prompt(
                id = 2L,
                title = "Simple Prompt",
                promptText = "Create a simple image",
                categoryId = 1L,
                category = testCategory,
                active = true,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(2L, result.id)
        assertEquals("Simple Prompt", result.title)
        assertEquals("Create a simple image", result.promptText)
        assertEquals(1L, result.categoryId)
        assertEquals(0, result.slots.size)
        assertNull(result.exampleImageUrl)
    }

    @Test
    fun `toDto should handle null category and subcategory`() {
        // Given
        val entity =
            Prompt(
                id = 3L,
                title = "Uncategorized Prompt",
                promptText = "Create an image",
                active = true,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(3L, result.id)
        assertEquals("Uncategorized Prompt", result.title)
        assertNull(result.categoryId)
        assertNull(result.category)
        assertNull(result.subcategoryId)
        assertNull(result.subcategory)
        assertEquals(true, result.active)
    }

    @Test
    fun `toDto should handle null optional fields`() {
        // Given
        val entity =
            Prompt(
                id = 4L,
                title = "Minimal Prompt",
                promptText = null,
                categoryId = 1L,
                category = testCategory,
                active = false,
                exampleImageFilename = null,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(4L, result.id)
        assertEquals("Minimal Prompt", result.title)
        assertNull(result.promptText)
        assertEquals(1L, result.categoryId)
        assertEquals(false, result.active)
        assertNull(result.exampleImageUrl)

        verify(exactly = 0) { imageService.getUrl(any(), any()) }
    }

    @Test
    fun `toDto should handle null timestamps correctly`() {
        // Given
        val entity =
            Prompt(
                id = 5L,
                title = "Timestamp Test Prompt",
                categoryId = 1L,
                category = testCategory,
                active = true,
                createdAt = null,
                updatedAt = null,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(5L, result.id)
        assertEquals("Timestamp Test Prompt", result.title)
        assertNull(result.createdAt)
        assertNull(result.updatedAt)
    }

    @Test
    fun `toDto should throw IllegalArgumentException when entity id is null`() {
        // Given
        val entity =
            Prompt(
                id = null,
                title = "Invalid Prompt",
                active = true,
            )

        // When/Then
        val exception =
            assertThrows<IllegalArgumentException> {
                assembler.toDto(entity)
            }

        assertEquals("Prompt ID cannot be null when converting to DTO", exception.message)
    }

    @Test
    fun `toDto should handle multiple slot variants correctly`() {
        // Given
        val entity = createMultiSlotPromptEntity()
        val slotVariant2 = createSecondSlotVariant()
        val (mapping1, mapping2) = createMultipleSlotVariantMappings(entity, slotVariant2)
        entity.promptSlotVariantMappings.addAll(listOf(mapping1, mapping2))

        val (expectedDto1, expectedDto2) = createExpectedMultipleSlotDtos()
        setupMultipleSlotVariantMocks(slotVariant2, expectedDto1, expectedDto2)

        // When
        val result = assembler.toDto(entity)

        // Then
        verifyMultipleSlotVariantsFields(result, expectedDto1, expectedDto2)
        verifyMultipleSlotVariantsInteractions(slotVariant2)
    }

    @Test
    fun `toPublicDto should convert entity to public DTO with correct fields only`() {
        // Given
        val exampleImageFilename = "public-prompt-example.jpg"
        val expectedImageUrl = "https://cdn.example.com/prompts/public-prompt-example.jpg"

        val entity =
            Prompt(
                id = 7L,
                title = "Public Prompt",
                promptText = "This should not appear in public DTO",
                categoryId = 1L,
                category = testCategory,
                subcategoryId = 2L,
                subcategory = testSubcategory,
                active = true,
                exampleImageFilename = exampleImageFilename,
            )

        // Add slot variant mapping
        val mapping =
            PromptSlotVariantMapping(
                id = PromptSlotVariantMappingId(7L, 1L),
                prompt = entity,
                promptSlotVariant = testSlotVariant,
            )
        entity.promptSlotVariantMappings.add(mapping)

        val expectedPublicSlotDto =
            PublicPromptSlotDto(
                id = 1L,
                name = "Nature Background",
                description = "Perfect for nature themes",
                exampleImageUrl = null,
                slotType = null,
            )

        every {
            imageService.getUrl(
                exampleImageFilename,
                ImageType.PROMPT_EXAMPLE,
            )
        } returns expectedImageUrl
        every { promptSlotVariantAssembler.toPublicDto(testSlotVariant) } returns expectedPublicSlotDto

        // When
        val result = assembler.toPublicDto(entity)

        // Then
        assertEquals(7L, result.id)
        assertEquals("Public Prompt", result.title)
        assertEquals(expectedImageUrl, result.exampleImageUrl)
        assertNotNull(result.category)
        assertEquals(1L, result.category!!.id)
        assertEquals("Test Category", result.category!!.name)
        assertNotNull(result.subcategory)
        assertEquals(2L, result.subcategory!!.id)
        assertEquals("Test Subcategory", result.subcategory!!.name)
        assertEquals(1, result.slots.size)
        assertEquals(expectedPublicSlotDto, result.slots[0])

        verify { imageService.getUrl(exampleImageFilename, ImageType.PROMPT_EXAMPLE) }
        verify { promptSlotVariantAssembler.toPublicDto(testSlotVariant) }
    }

    @Test
    fun `toPublicDto should handle null category and subcategory`() {
        // Given
        val entity =
            Prompt(
                id = 8L,
                title = "Public Uncategorized Prompt",
                active = true,
            )

        // When
        val result = assembler.toPublicDto(entity)

        // Then
        assertEquals(8L, result.id)
        assertEquals("Public Uncategorized Prompt", result.title)
        assertNull(result.category)
        assertNull(result.subcategory)
        assertNull(result.exampleImageUrl)
        assertEquals(0, result.slots.size)
    }

    @Test
    fun `toPublicDto should handle null exampleImageFilename correctly`() {
        // Given
        val entity =
            Prompt(
                id = 9L,
                title = "Public Prompt Without Image",
                categoryId = 1L,
                category = testCategory,
                active = true,
                exampleImageFilename = null,
            )

        // When
        val result = assembler.toPublicDto(entity)

        // Then
        assertEquals(9L, result.id)
        assertEquals("Public Prompt Without Image", result.title)
        assertNull(result.exampleImageUrl)
        assertNotNull(result.category)

        verify(exactly = 0) { imageService.getUrl(any(), any()) }
    }

    @Test
    fun `toPublicDto should throw IllegalArgumentException when entity id is null`() {
        // Given
        val entity =
            Prompt(
                id = null,
                title = "Invalid Public Prompt",
                active = true,
            )

        // When/Then
        val exception =
            assertThrows<IllegalArgumentException> {
                assembler.toPublicDto(entity)
            }

        assertEquals("Prompt ID cannot be null when converting to DTO", exception.message)
    }

    @Test
    fun `toPublicDto should handle prompt without slots`() {
        // Given
        val entity =
            Prompt(
                id = 10L,
                title = "Simple Public Prompt",
                categoryId = 1L,
                category = testCategory,
                active = true,
            )

        // When
        val result = assembler.toPublicDto(entity)

        // Then
        assertEquals(10L, result.id)
        assertEquals("Simple Public Prompt", result.title)
        assertEquals(0, result.slots.size)
        assertNotNull(result.category)
    }

    @Test
    fun `both conversion methods should use same StoragePathService call`() {
        // Given
        val filename = "shared-example.jpg"
        val expectedUrl = "https://storage.example.com/shared-example.jpg"
        val entity =
            Prompt(
                id = 11L,
                title = "Shared Example",
                categoryId = 1L,
                category = testCategory,
                active = true,
                exampleImageFilename = filename,
            )

        every { imageService.getUrl(filename, ImageType.PROMPT_EXAMPLE) } returns expectedUrl

        // When
        val dtoResult = assembler.toDto(entity)
        val publicDtoResult = assembler.toPublicDto(entity)

        // Then
        assertEquals(expectedUrl, dtoResult.exampleImageUrl)
        assertEquals(expectedUrl, publicDtoResult.exampleImageUrl)
        verify(exactly = 2) { imageService.getUrl(filename, ImageType.PROMPT_EXAMPLE) }
    }

    @Test
    fun `toDto should handle long text values correctly`() {
        // Given
        val longTitle = "A" + "very ".repeat(50) + "long title that exceeds normal limits"
        val longPromptText = "A" + "very ".repeat(100) + "long prompt text that exceeds normal limits"

        val entity =
            Prompt(
                id = 12L,
                title = longTitle,
                promptText = longPromptText,
                categoryId = 1L,
                category = testCategory,
                active = true,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(longTitle, result.title)
        assertEquals(longPromptText, result.promptText)
    }

    @Test
    fun `conversion methods should handle complex category scenarios`() {
        // Given
        val complexCategory =
            PromptCategory(
                id = 999L,
                name = "Complex & Special Characters",
            )

        val complexSubcategory =
            PromptSubCategory(
                id = 888L,
                name = "Complex Sub & Category",
                description = "Subcategory with special chars",
                promptCategory = complexCategory,
            )

        val entity =
            Prompt(
                id = 13L,
                title = "Complex Categorized Prompt",
                categoryId = 999L,
                category = complexCategory,
                subcategoryId = 888L,
                subcategory = complexSubcategory,
                active = true,
                exampleImageFilename = "complex.webp",
            )

        val expectedUrl = "https://example.com/complex.webp"
        every { imageService.getUrl("complex.webp", ImageType.PROMPT_EXAMPLE) } returns expectedUrl

        // When
        val dtoResult = assembler.toDto(entity)
        val publicResult = assembler.toPublicDto(entity)

        // Then
        assertEquals(999L, dtoResult.category!!.id)
        assertEquals("Complex & Special Characters", dtoResult.category!!.name)
        assertEquals(888L, dtoResult.subcategory!!.id)
        assertEquals("Complex Sub & Category", dtoResult.subcategory!!.name)

        assertEquals(999L, publicResult.category!!.id)
        assertEquals("Complex & Special Characters", publicResult.category!!.name)
        assertEquals(888L, publicResult.subcategory!!.id)
        assertEquals("Complex Sub & Category", publicResult.subcategory!!.name)
    }

    // Helper methods for full DTO conversion test
    private fun createTestPromptWithAllFields(
        createdAt: OffsetDateTime,
        updatedAt: OffsetDateTime,
        exampleImageFilename: String,
    ) = Prompt(
        id = 1L,
        title = "Test Prompt",
        promptText = "Create a beautiful image with {background}",
        categoryId = 1L,
        category = testCategory,
        subcategoryId = 2L,
        subcategory = testSubcategory,
        active = true,
        exampleImageFilename = exampleImageFilename,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun createTestSlotVariantMapping(entity: Prompt) =
        PromptSlotVariantMapping(
            id = PromptSlotVariantMappingId(1L, 1L),
            prompt = entity,
            promptSlotVariant = testSlotVariant,
        )

    private fun createExpectedSlotDto() =
        PromptSlotVariantDto(
            id = 1L,
            promptSlotTypeId = 1L,
            promptSlotType = null,
            name = "Nature Background",
            prompt = "A beautiful natural landscape",
            description = "Perfect for nature themes",
            exampleImageUrl = null,
            createdAt = null,
            updatedAt = null,
        )

    private fun setupPromptTestMocks(
        exampleImageFilename: String,
        expectedImageUrl: String,
        expectedSlotDto: PromptSlotVariantDto,
    ) {
        every {
            imageService.getUrl(
                exampleImageFilename,
                ImageType.PROMPT_EXAMPLE,
            )
        } returns expectedImageUrl
        every { promptSlotVariantAssembler.toDto(testSlotVariant) } returns expectedSlotDto
    }

    private fun verifyPromptDtoFields(
        result: com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto,
        expectedSlotDto: PromptSlotVariantDto,
        expectedImageUrl: String,
        createdAt: OffsetDateTime,
        updatedAt: OffsetDateTime,
    ) {
        assertEquals(1L, result.id)
        assertEquals("Test Prompt", result.title)
        assertEquals("Create a beautiful image with {background}", result.promptText)
        assertEquals(1L, result.categoryId)
        assertNotNull(result.category)
        assertEquals(1L, result.category!!.id)
        assertEquals("Test Category", result.category!!.name)
        assertEquals(2L, result.subcategoryId)
        assertNotNull(result.subcategory)
        assertEquals(2L, result.subcategory!!.id)
        assertEquals("Test Subcategory", result.subcategory!!.name)
        assertEquals(true, result.active)
        assertEquals(1, result.slots.size)
        assertEquals(expectedSlotDto, result.slots[0])
        assertEquals(expectedImageUrl, result.exampleImageUrl)
        assertEquals(createdAt, result.createdAt)
        assertEquals(updatedAt, result.updatedAt)
    }

    private fun verifyPromptTestInteractions(exampleImageFilename: String) {
        verify { imageService.getUrl(exampleImageFilename, ImageType.PROMPT_EXAMPLE) }
        verify { promptSlotVariantAssembler.toDto(testSlotVariant) }
    }

    // Helper methods for multiple slot variants test
    private fun createMultiSlotPromptEntity() =
        Prompt(
            id = 6L,
            title = "Multi-Slot Prompt",
            promptText = "Create an image with {background} and {style}",
            categoryId = 1L,
            category = testCategory,
            active = true,
        )

    private fun createSecondSlotVariant() =
        PromptSlotVariant(
            id = 2L,
            promptSlotTypeId = 2L,
            name = "Artistic Style",
            prompt = "Abstract art style",
        )

    private fun createMultipleSlotVariantMappings(
        entity: Prompt,
        slotVariant2: PromptSlotVariant,
    ): Pair<PromptSlotVariantMapping, PromptSlotVariantMapping> {
        val mapping1 =
            PromptSlotVariantMapping(
                id = PromptSlotVariantMappingId(6L, 1L),
                prompt = entity,
                promptSlotVariant = testSlotVariant,
            )

        val mapping2 =
            PromptSlotVariantMapping(
                id = PromptSlotVariantMappingId(6L, 2L),
                prompt = entity,
                promptSlotVariant = slotVariant2,
            )

        return Pair(mapping1, mapping2)
    }

    private fun createExpectedMultipleSlotDtos(): Pair<PromptSlotVariantDto, PromptSlotVariantDto> {
        val expectedDto1 =
            PromptSlotVariantDto(
                id = 1L,
                promptSlotTypeId = 1L,
                promptSlotType = null,
                name = "Nature Background",
                prompt = "A beautiful natural landscape",
                description = "Perfect for nature themes",
                exampleImageUrl = null,
                createdAt = null,
                updatedAt = null,
            )

        val expectedDto2 =
            PromptSlotVariantDto(
                id = 2L,
                promptSlotTypeId = 2L,
                promptSlotType = null,
                name = "Artistic Style",
                prompt = "Abstract art style",
                description = null,
                exampleImageUrl = null,
                createdAt = null,
                updatedAt = null,
            )

        return Pair(expectedDto1, expectedDto2)
    }

    private fun setupMultipleSlotVariantMocks(
        slotVariant2: PromptSlotVariant,
        expectedDto1: PromptSlotVariantDto,
        expectedDto2: PromptSlotVariantDto,
    ) {
        every { promptSlotVariantAssembler.toDto(testSlotVariant) } returns expectedDto1
        every { promptSlotVariantAssembler.toDto(slotVariant2) } returns expectedDto2
    }

    private fun verifyMultipleSlotVariantsFields(
        result: com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto,
        expectedDto1: PromptSlotVariantDto,
        expectedDto2: PromptSlotVariantDto,
    ) {
        assertEquals(6L, result.id)
        assertEquals(2, result.slots.size)
        assertEquals(expectedDto1, result.slots[0])
        assertEquals(expectedDto2, result.slots[1])
    }

    private fun verifyMultipleSlotVariantsInteractions(slotVariant2: PromptSlotVariant) {
        verify { promptSlotVariantAssembler.toDto(testSlotVariant) }
        verify { promptSlotVariantAssembler.toDto(slotVariant2) }
    }
}
