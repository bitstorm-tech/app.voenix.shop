package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.prompt.internal.entity.PromptSlotType
import com.jotoai.voenix.shop.prompt.internal.entity.PromptSlotVariant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import java.time.OffsetDateTime

class PromptSlotVariantAssemblerTest {
    private lateinit var storagePathService: StoragePathService
    private lateinit var assembler: PromptSlotVariantAssembler

    private lateinit var testPromptSlotType: PromptSlotType

    @BeforeEach
    fun setUp() {
        storagePathService = mock(StoragePathService::class.java)
        assembler = PromptSlotVariantAssembler(storagePathService)

        // Setup test data
        testPromptSlotType =
            PromptSlotType(
                id = 1L,
                name = "Background",
                position = 1,
            )
    }

    @Test
    fun `toDto should convert entity to DTO with all fields properly mapped`() {
        // Given
        val createdAt = OffsetDateTime.now().minusDays(1)
        val updatedAt = OffsetDateTime.now()
        val exampleImageFilename = "background-example.jpg"
        val expectedImageUrl = "https://example.com/images/prompt-variants/background-example.jpg"

        val entity =
            PromptSlotVariant(
                id = 1L,
                promptSlotTypeId = 1L,
                promptSlotType = testPromptSlotType,
                name = "Nature Background",
                prompt = "A beautiful natural landscape with mountains and trees",
                description = "Perfect for nature-themed artwork",
                exampleImageFilename = exampleImageFilename,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )

        `when`(storagePathService.getImageUrl(ImageType.PROMPT_SLOT_VARIANT_EXAMPLE, exampleImageFilename))
            .thenReturn(expectedImageUrl)

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(1L, result.id)
        assertEquals(1L, result.promptSlotTypeId)
        assertNotNull(result.promptSlotType)
        assertEquals(1L, result.promptSlotType!!.id)
        assertEquals("Background", result.promptSlotType!!.name)
        assertEquals(1, result.promptSlotType!!.position)
        assertEquals("Nature Background", result.name)
        assertEquals("A beautiful natural landscape with mountains and trees", result.prompt)
        assertEquals("Perfect for nature-themed artwork", result.description)
        assertEquals(expectedImageUrl, result.exampleImageUrl)
        assertEquals(createdAt, result.createdAt)
        assertEquals(updatedAt, result.updatedAt)

        verify(storagePathService).getImageUrl(ImageType.PROMPT_SLOT_VARIANT_EXAMPLE, exampleImageFilename)
    }

    @Test
    fun `toDto should handle null promptSlotType correctly`() {
        // Given
        val entity =
            PromptSlotVariant(
                id = 2L,
                promptSlotTypeId = 2L,
                promptSlotType = null,
                name = "Abstract Art",
                prompt = "Create abstract geometric patterns",
                description = "Modern abstract style",
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(2L, result.id)
        assertEquals(2L, result.promptSlotTypeId)
        assertNull(result.promptSlotType)
        assertEquals("Abstract Art", result.name)
        assertEquals("Create abstract geometric patterns", result.prompt)
        assertEquals("Modern abstract style", result.description)
        assertNull(result.exampleImageUrl)
    }

    @Test
    fun `toDto should handle null optional fields correctly`() {
        // Given
        val entity =
            PromptSlotVariant(
                id = 3L,
                promptSlotTypeId = 1L,
                promptSlotType = testPromptSlotType,
                name = "Minimal Style",
                prompt = null,
                description = null,
                exampleImageFilename = null,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(3L, result.id)
        assertEquals("Minimal Style", result.name)
        assertNull(result.prompt)
        assertNull(result.description)
        assertNull(result.exampleImageUrl)

        // Verify StoragePathService has no interactions when filename is null
        verifyNoInteractions(storagePathService)
    }

    @Test
    fun `toDto should handle null timestamps correctly`() {
        // Given
        val entity =
            PromptSlotVariant(
                id = 4L,
                promptSlotTypeId = 1L,
                promptSlotType = testPromptSlotType,
                name = "Vintage Style",
                createdAt = null,
                updatedAt = null,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(4L, result.id)
        assertEquals("Vintage Style", result.name)
        assertNull(result.createdAt)
        assertNull(result.updatedAt)
    }

    @Test
    fun `toDto should throw IllegalArgumentException when entity id is null`() {
        // Given
        val entity =
            PromptSlotVariant(
                id = null,
                promptSlotTypeId = 1L,
                name = "Invalid Variant",
            )

        // When/Then
        val exception =
            assertThrows<IllegalArgumentException> {
                assembler.toDto(entity)
            }

        assertEquals("PromptSlotVariant ID cannot be null when converting to DTO", exception.message)
    }

    @Test
    fun `toPublicDto should convert entity to public DTO with correct fields`() {
        // Given
        val exampleImageFilename = "public-example.png"
        val expectedImageUrl = "https://cdn.example.com/prompts/public-example.png"

        val entity =
            PromptSlotVariant(
                id = 5L,
                promptSlotTypeId = 1L,
                promptSlotType = testPromptSlotType,
                name = "Public Variant",
                prompt = "This should not appear in public DTO",
                description = "This is a public description",
                exampleImageFilename = exampleImageFilename,
            )

        `when`(storagePathService.getImageUrl(ImageType.PROMPT_SLOT_VARIANT_EXAMPLE, exampleImageFilename))
            .thenReturn(expectedImageUrl)

        // When
        val result = assembler.toPublicDto(entity)

        // Then
        assertEquals(5L, result.id)
        assertEquals("Public Variant", result.name)
        assertEquals("This is a public description", result.description)
        assertEquals(expectedImageUrl, result.exampleImageUrl)
        assertNotNull(result.slotType)
        assertEquals(1L, result.slotType!!.id)
        assertEquals("Background", result.slotType!!.name)
        assertEquals(1, result.slotType!!.position)

        verify(storagePathService).getImageUrl(ImageType.PROMPT_SLOT_VARIANT_EXAMPLE, exampleImageFilename)
    }

    @Test
    fun `toPublicDto should handle null promptSlotType correctly`() {
        // Given
        val entity =
            PromptSlotVariant(
                id = 6L,
                promptSlotTypeId = 2L,
                promptSlotType = null,
                name = "Public Without Type",
                description = "Public description without slot type",
            )

        // When
        val result = assembler.toPublicDto(entity)

        // Then
        assertEquals(6L, result.id)
        assertEquals("Public Without Type", result.name)
        assertEquals("Public description without slot type", result.description)
        assertNull(result.exampleImageUrl)
        assertNull(result.slotType)
    }

    @Test
    fun `toPublicDto should handle null optional fields correctly`() {
        // Given
        val entity =
            PromptSlotVariant(
                id = 7L,
                promptSlotTypeId = 1L,
                promptSlotType = testPromptSlotType,
                name = "Minimal Public",
                description = null,
                exampleImageFilename = null,
            )

        // When
        val result = assembler.toPublicDto(entity)

        // Then
        assertEquals(7L, result.id)
        assertEquals("Minimal Public", result.name)
        assertNull(result.description)
        assertNull(result.exampleImageUrl)
        assertNotNull(result.slotType)

        // Verify StoragePathService has no interactions when filename is null
        verifyNoInteractions(storagePathService)
    }

    @Test
    fun `toPublicDto should throw IllegalArgumentException when entity id is null`() {
        // Given
        val entity =
            PromptSlotVariant(
                id = null,
                promptSlotTypeId = 1L,
                name = "Invalid Public Variant",
            )

        // When/Then
        val exception =
            assertThrows<IllegalArgumentException> {
                assembler.toPublicDto(entity)
            }

        assertEquals("PromptSlotVariant ID cannot be null when converting to DTO", exception.message)
    }

    @Test
    fun `toDto and toPublicDto should call StoragePathService with same parameters`() {
        // Given
        val filename = "shared-example.jpg"
        val expectedUrl = "https://storage.example.com/shared-example.jpg"
        val entity =
            PromptSlotVariant(
                id = 8L,
                promptSlotTypeId = 1L,
                promptSlotType = testPromptSlotType,
                name = "Shared Example",
                exampleImageFilename = filename,
            )

        `when`(storagePathService.getImageUrl(ImageType.PROMPT_SLOT_VARIANT_EXAMPLE, filename))
            .thenReturn(expectedUrl)

        // When
        val dtoResult = assembler.toDto(entity)
        val publicDtoResult = assembler.toPublicDto(entity)

        // Then
        assertEquals(expectedUrl, dtoResult.exampleImageUrl)
        assertEquals(expectedUrl, publicDtoResult.exampleImageUrl)
        verify(storagePathService, times(2)).getImageUrl(ImageType.PROMPT_SLOT_VARIANT_EXAMPLE, filename)
    }

    @Test
    fun `toDto should handle long text values correctly`() {
        // Given
        val longPrompt = "A" + "very ".repeat(100) + "long prompt text that exceeds normal limits"
        val longDescription = "A" + "very ".repeat(50) + "long description text"

        val entity =
            PromptSlotVariant(
                id = 9L,
                promptSlotTypeId = 1L,
                promptSlotType = testPromptSlotType,
                name = "Long Text Variant",
                prompt = longPrompt,
                description = longDescription,
            )

        // When
        val result = assembler.toDto(entity)

        // Then
        assertEquals(longPrompt, result.prompt)
        assertEquals(longDescription, result.description)
    }

    @Test
    fun `both conversion methods should handle complex promptSlotType scenarios`() {
        // Given
        val complexPromptSlotType =
            PromptSlotType(
                id = 999L,
                name = "Complex & Special Characters",
                position = 42,
            )

        val entity =
            PromptSlotVariant(
                id = 10L,
                promptSlotTypeId = 999L,
                promptSlotType = complexPromptSlotType,
                name = "Complex Variant",
                exampleImageFilename = "complex.webp",
            )

        val expectedUrl = "https://example.com/complex.webp"
        `when`(storagePathService.getImageUrl(ImageType.PROMPT_SLOT_VARIANT_EXAMPLE, "complex.webp"))
            .thenReturn(expectedUrl)

        // When
        val dtoResult = assembler.toDto(entity)
        val publicResult = assembler.toPublicDto(entity)

        // Then
        assertEquals(999L, dtoResult.promptSlotType!!.id)
        assertEquals("Complex & Special Characters", dtoResult.promptSlotType!!.name)
        assertEquals(42, dtoResult.promptSlotType!!.position)

        assertEquals(999L, publicResult.slotType!!.id)
        assertEquals("Complex & Special Characters", publicResult.slotType!!.name)
        assertEquals(42, publicResult.slotType!!.position)
    }
}
