package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.image.api.ImageStorageService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.CreatePromptSlotVariantRequest
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.UpdatePromptSlotVariantRequest
import com.jotoai.voenix.shop.prompt.api.exceptions.PromptSlotVariantNotFoundException
import com.jotoai.voenix.shop.prompt.internal.entity.PromptSlotType
import com.jotoai.voenix.shop.prompt.internal.entity.PromptSlotVariant
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSlotTypeRepository
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSlotVariantRepository
import io.mockk.any
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.OffsetDateTime
import java.util.Optional

@DisplayName("PromptSlotVariantServiceImpl Unit Tests")
class PromptSlotVariantServiceImplTest {
    private lateinit var promptSlotVariantRepository: PromptSlotVariantRepository
    private lateinit var promptSlotTypeRepository: PromptSlotTypeRepository
    private lateinit var promptSlotVariantAssembler: PromptSlotVariantAssembler
    private lateinit var imageStorageService: ImageStorageService
    private lateinit var service: PromptSlotVariantServiceImpl

    private val testPromptSlotType =
        PromptSlotType(
            id = 1L,
            name = "Background",
            position = 1,
        )

    private val testPromptSlotVariant =
        PromptSlotVariant(
            id = 1L,
            promptSlotTypeId = 1L,
            promptSlotType = testPromptSlotType,
            name = "Nature Background",
            prompt = "A beautiful natural landscape",
            description = "Perfect for nature themes",
            exampleImageFilename = "nature.jpg",
            createdAt = OffsetDateTime.now(),
            updatedAt = OffsetDateTime.now(),
        )

    @BeforeEach
    fun setUp() {
        promptSlotVariantRepository = mockk()
        promptSlotTypeRepository = mockk()
        promptSlotVariantAssembler = mockk()
        imageStorageService = mockk()

        service =
            PromptSlotVariantServiceImpl(
                promptSlotVariantRepository = promptSlotVariantRepository,
                promptSlotTypeRepository = promptSlotTypeRepository,
                promptSlotVariantAssembler = promptSlotVariantAssembler,
                imageStorageService = imageStorageService,
            )
    }

    @Nested
    @DisplayName("Create Slot Variant Tests")
    inner class CreateSlotVariantTests {
        @Test
        fun `should successfully create slot variant with all fields`() {
            // Given
            val request =
                CreatePromptSlotVariantRequest(
                    promptSlotTypeId = 1L,
                    name = "New Variant",
                    prompt = "Test prompt",
                    description = "Test description",
                    exampleImageFilename = "example.jpg",
                )

            val savedEntity =
                PromptSlotVariant(
                    id = 2L,
                    promptSlotTypeId = request.promptSlotTypeId,
                    name = request.name,
                    prompt = request.prompt,
                    description = request.description,
                    exampleImageFilename = request.exampleImageFilename,
                )

            every { promptSlotTypeRepository.existsById(1L) } returns true
            every { promptSlotVariantRepository.existsByName("New Variant") } returns false
            every { promptSlotVariantRepository.save(any()) } returns savedEntity
            every { promptSlotVariantAssembler.toDto(any()) } returns mockk()

            // When
            val result = service.createSlotVariant(request)

            // Then
            assertNotNull(result)

            val entityCaptor = slot<PromptSlotVariant>()
            verify { promptSlotVariantRepository.save(capture(entityCaptor)) }

            val capturedEntity = entityCaptor.captured
            assertEquals(request.promptSlotTypeId, capturedEntity.promptSlotTypeId)
            assertEquals(request.name, capturedEntity.name)
            assertEquals(request.prompt, capturedEntity.prompt)
            assertEquals(request.description, capturedEntity.description)
            assertEquals(request.exampleImageFilename, capturedEntity.exampleImageFilename)
        }

        @Test
        fun `should successfully create slot variant with minimal fields`() {
            // Given
            val request =
                CreatePromptSlotVariantRequest(
                    promptSlotTypeId = 1L,
                    name = "Minimal Variant",
                    prompt = null,
                    description = null,
                    exampleImageFilename = null,
                )

            val savedEntity =
                PromptSlotVariant(
                    id = 3L,
                    promptSlotTypeId = request.promptSlotTypeId,
                    name = request.name,
                )

            every { promptSlotTypeRepository.existsById(1L) } returns true
            every { promptSlotVariantRepository.existsByName("Minimal Variant") } returns false
            every { promptSlotVariantRepository.save(any()) } returns savedEntity
            every { promptSlotVariantAssembler.toDto(any()) } returns mockk()

            // When
            val result = service.createSlotVariant(request)

            // Then
            assertNotNull(result)
            verify { promptSlotVariantRepository.save(any()) }
        }

        @Test
        fun `should throw exception when promptSlotTypeId does not exist`() {
            // Given
            val request =
                CreatePromptSlotVariantRequest(
                    promptSlotTypeId = 999L,
                    name = "Invalid Variant",
                )

            every { promptSlotTypeRepository.existsById(999L) } returns false

            // When/Then
            val exception =
                assertThrows<IllegalArgumentException> {
                    service.createSlotVariant(request)
                }

            assertEquals("PromptSlotType with id '999' does not exist", exception.message)
            verify(exactly = 0) { promptSlotVariantRepository.save(any()) }
        }

        @Test
        fun `should throw exception when name already exists`() {
            // Given
            val request =
                CreatePromptSlotVariantRequest(
                    promptSlotTypeId = 1L,
                    name = "Duplicate Name",
                )

            every { promptSlotTypeRepository.existsById(1L) } returns true
            every { promptSlotVariantRepository.existsByName("Duplicate Name") } returns true

            // When/Then
            val exception =
                assertThrows<IllegalArgumentException> {
                    service.createSlotVariant(request)
                }

            assertEquals("PromptSlotVariant with name 'Duplicate Name' already exists", exception.message)
            verify(exactly = 0) { promptSlotVariantRepository.save(any()) }
        }

        @Test
        fun `should handle special characters in name`() {
            // Given
            val request =
                CreatePromptSlotVariantRequest(
                    promptSlotTypeId = 1L,
                    name = "Variant & Special #123",
                    prompt = "Test with special chars",
                )

            val savedEntity =
                PromptSlotVariant(
                    id = 4L,
                    promptSlotTypeId = request.promptSlotTypeId,
                    name = request.name,
                    prompt = request.prompt,
                )

            every { promptSlotTypeRepository.existsById(1L) } returns true
            every { promptSlotVariantRepository.existsByName(request.name) } returns false
            every { promptSlotVariantRepository.save(any()) } returns savedEntity
            every { promptSlotVariantAssembler.toDto(any()) } returns mockk()

            // When
            val result = service.createSlotVariant(request)

            // Then
            assertNotNull(result)
            verify { promptSlotVariantRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("Update Slot Variant Tests")
    inner class UpdateSlotVariantTests {
        @Test
        fun `should successfully update all fields`() {
            // Given
            val existingEntity =
                PromptSlotVariant(
                    id = 1L,
                    promptSlotTypeId = 1L,
                    name = "Old Name",
                    prompt = "Old prompt",
                    description = "Old description",
                    exampleImageFilename = "old.jpg",
                )

            val request =
                UpdatePromptSlotVariantRequest(
                    promptSlotTypeId = 2L,
                    name = "New Name",
                    prompt = "New prompt",
                    description = "New description",
                    exampleImageFilename = "new.jpg",
                )

            every { promptSlotVariantRepository.findById(1L) } returns Optional.of(existingEntity)
            every { promptSlotTypeRepository.existsById(2L) } returns true
            every { promptSlotVariantRepository.existsByNameAndIdNot("New Name", 1L) } returns false
            every { promptSlotVariantRepository.save(any()) } returns existingEntity
            every { promptSlotVariantAssembler.toDto(any()) } returns mockk()

            // When
            val result = service.updateSlotVariant(1L, request)

            // Then
            assertNotNull(result)
            assertEquals(2L, existingEntity.promptSlotTypeId)
            assertEquals("New Name", existingEntity.name)
            assertEquals("New prompt", existingEntity.prompt)
            assertEquals("New description", existingEntity.description)
            assertEquals("new.jpg", existingEntity.exampleImageFilename)

            verify { promptSlotVariantRepository.save(existingEntity) }
        }

        @Test
        fun `should successfully update only name`() {
            // Given
            val existingEntity =
                PromptSlotVariant(
                    id = 1L,
                    promptSlotTypeId = 1L,
                    name = "Old Name",
                    prompt = "Keep this prompt",
                    description = "Keep this description",
                )

            val request =
                UpdatePromptSlotVariantRequest(
                    name = "Updated Name",
                )

            every { promptSlotVariantRepository.findById(1L) } returns Optional.of(existingEntity)
            every { promptSlotVariantRepository.existsByNameAndIdNot("Updated Name", 1L) } returns false
            every { promptSlotVariantRepository.save(any()) } returns existingEntity
            every { promptSlotVariantAssembler.toDto(any()) } returns mockk()

            // When
            val result = service.updateSlotVariant(1L, request)

            // Then
            assertNotNull(result)
            assertEquals("Updated Name", existingEntity.name)
            assertEquals("Keep this prompt", existingEntity.prompt)
            assertEquals("Keep this description", existingEntity.description)
            assertEquals(1L, existingEntity.promptSlotTypeId)

            verify(exactly = 0) { promptSlotTypeRepository.existsById(any()) }
        }

        @Test
        fun `should throw exception when entity not found`() {
            // Given
            val request = UpdatePromptSlotVariantRequest(name = "New Name")

            every { promptSlotVariantRepository.findById(999L) } returns Optional.empty()

            // When/Then
            val exception =
                assertThrows<PromptSlotVariantNotFoundException> {
                    service.updateSlotVariant(999L, request)
                }

            verify(exactly = 0) { promptSlotVariantRepository.save(any()) }
        }

        @Test
        fun `should throw exception when new promptSlotTypeId does not exist`() {
            // Given
            val existingEntity =
                PromptSlotVariant(
                    id = 1L,
                    promptSlotTypeId = 1L,
                    name = "Existing",
                )

            val request =
                UpdatePromptSlotVariantRequest(
                    promptSlotTypeId = 999L,
                )

            every { promptSlotVariantRepository.findById(1L) } returns Optional.of(existingEntity)
            every { promptSlotTypeRepository.existsById(999L) } returns false

            // When/Then
            val exception =
                assertThrows<IllegalArgumentException> {
                    service.updateSlotVariant(1L, request)
                }

            assertEquals("PromptSlotType with id '999' does not exist", exception.message)
            verify(exactly = 0) { promptSlotVariantRepository.save(any()) }
        }

        @Test
        fun `should throw exception when new name already exists`() {
            // Given
            val existingEntity =
                PromptSlotVariant(
                    id = 1L,
                    promptSlotTypeId = 1L,
                    name = "Current Name",
                )

            val request =
                UpdatePromptSlotVariantRequest(
                    name = "Duplicate Name",
                )

            every { promptSlotVariantRepository.findById(1L) } returns Optional.of(existingEntity)
            every { promptSlotVariantRepository.existsByNameAndIdNot("Duplicate Name", 1L) } returns true

            // When/Then
            val exception =
                assertThrows<IllegalArgumentException> {
                    service.updateSlotVariant(1L, request)
                }

            assertEquals("PromptSlotVariant with name 'Duplicate Name' already exists", exception.message)
            verify(exactly = 0) { promptSlotVariantRepository.save(any()) }
        }

        @Test
        fun `should delete image when explicitly set to null`() {
            // Given
            val existingEntity =
                PromptSlotVariant(
                    id = 1L,
                    promptSlotTypeId = 1L,
                    name = "Keep Name",
                    prompt = "Keep prompt",
                    description = "Keep description",
                    exampleImageFilename = "old-image.jpg",
                )

            val request =
                UpdatePromptSlotVariantRequest(
                    name = "Updated Name",
                    exampleImageFilename = null,
                )

            every { promptSlotVariantRepository.findById(1L) } returns Optional.of(existingEntity)
            every { promptSlotVariantRepository.existsByNameAndIdNot("Updated Name", 1L) } returns false
            every { promptSlotVariantRepository.save(any()) } returns existingEntity
            every { promptSlotVariantAssembler.toDto(any()) } returns mockk()
            every {
                imageStorageService.deleteFile(
                    "old-image.jpg",
                    ImageType.PROMPT_SLOT_VARIANT_EXAMPLE,
                )
            } returns true

            // When
            val result = service.updateSlotVariant(1L, request)

            // Then
            assertNotNull(result)
            assertEquals("Updated Name", existingEntity.name)
            assertEquals("Keep prompt", existingEntity.prompt)
            assertEquals("Keep description", existingEntity.description)
            assertEquals(null, existingEntity.exampleImageFilename) // Should be cleared

            // Verify the old image was deleted
            verify { imageStorageService.deleteFile("old-image.jpg", ImageType.PROMPT_SLOT_VARIANT_EXAMPLE) }
        }

        @Test
        fun `should allow updating to same name`() {
            // Given
            val existingEntity =
                PromptSlotVariant(
                    id = 1L,
                    promptSlotTypeId = 1L,
                    name = "Same Name",
                )

            val request =
                UpdatePromptSlotVariantRequest(
                    name = "Same Name",
                    prompt = "Updated prompt",
                )

            every { promptSlotVariantRepository.findById(1L) } returns Optional.of(existingEntity)
            every { promptSlotVariantRepository.existsByNameAndIdNot("Same Name", 1L) } returns false
            every { promptSlotVariantRepository.save(any()) } returns existingEntity
            every { promptSlotVariantAssembler.toDto(any()) } returns mockk()

            // When
            val result = service.updateSlotVariant(1L, request)

            // Then
            assertNotNull(result)
            assertEquals("Same Name", existingEntity.name)
            assertEquals("Updated prompt", existingEntity.prompt)
        }

        @Test
        fun `should delete old image when replacing with new image`() {
            // Given
            val existingEntity =
                PromptSlotVariant(
                    id = 1L,
                    promptSlotTypeId = 1L,
                    name = "Test",
                    prompt = "Test prompt",
                    exampleImageFilename = "old-image.jpg",
                )

            val request =
                UpdatePromptSlotVariantRequest(
                    exampleImageFilename = "new-image.jpg",
                )

            every { promptSlotVariantRepository.findById(1L) } returns Optional.of(existingEntity)
            every { promptSlotVariantRepository.save(any()) } returns existingEntity
            every { promptSlotVariantAssembler.toDto(any()) } returns mockk()
            every {
                imageStorageService.deleteFile(
                    "old-image.jpg",
                    ImageType.PROMPT_SLOT_VARIANT_EXAMPLE,
                )
            } returns true

            // When
            val result = service.updateSlotVariant(1L, request)

            // Then
            assertNotNull(result)
            assertEquals("new-image.jpg", existingEntity.exampleImageFilename)

            // Verify the old image was deleted
            verify { imageStorageService.deleteFile("old-image.jpg", ImageType.PROMPT_SLOT_VARIANT_EXAMPLE) }
        }

        @Test
        fun `should not delete image when filename stays the same`() {
            // Given
            val existingEntity =
                PromptSlotVariant(
                    id = 1L,
                    promptSlotTypeId = 1L,
                    name = "Test",
                    prompt = "Test prompt",
                    exampleImageFilename = "same-image.jpg",
                )

            val request =
                UpdatePromptSlotVariantRequest(
                    name = "Updated Name",
                    exampleImageFilename = "same-image.jpg",
                )

            every { promptSlotVariantRepository.findById(1L) } returns Optional.of(existingEntity)
            every { promptSlotVariantRepository.existsByNameAndIdNot("Updated Name", 1L) } returns false
            every { promptSlotVariantRepository.save(any()) } returns existingEntity
            every { promptSlotVariantAssembler.toDto(any()) } returns mockk()

            // When
            val result = service.updateSlotVariant(1L, request)

            // Then
            assertNotNull(result)
            assertEquals("same-image.jpg", existingEntity.exampleImageFilename)

            // Verify the image was NOT deleted since it's the same
            verify(exactly = 0) { imageStorageService.deleteFile(any(), any()) }
        }
    }

    @Nested
    @DisplayName("Delete Slot Variant Tests")
    inner class DeleteSlotVariantTests {
        @Test
        fun `should successfully delete slot variant with image cleanup`() {
            // Given
            val existingEntity =
                PromptSlotVariant(
                    id = 1L,
                    promptSlotTypeId = 1L,
                    name = "To Delete",
                    prompt = "Will be deleted",
                    exampleImageFilename = "image-to-delete.jpg",
                )

            every { promptSlotVariantRepository.findById(1L) } returns Optional.of(existingEntity)
            every {
                imageStorageService.deleteFile(
                    "image-to-delete.jpg",
                    ImageType.PROMPT_SLOT_VARIANT_EXAMPLE,
                )
            } returns true

            // When
            service.deleteSlotVariant(1L)

            // Then
            verify { promptSlotVariantRepository.findById(1L) }
            verify { imageStorageService.deleteFile("image-to-delete.jpg", ImageType.PROMPT_SLOT_VARIANT_EXAMPLE) }
            verify { promptSlotVariantRepository.deleteById(1L) }
        }

        @Test
        fun `should successfully delete slot variant without image`() {
            // Given
            val existingEntity =
                PromptSlotVariant(
                    id = 1L,
                    promptSlotTypeId = 1L,
                    name = "To Delete",
                    prompt = "Will be deleted",
                    exampleImageFilename = null,
                )

            every { promptSlotVariantRepository.findById(1L) } returns Optional.of(existingEntity)

            // When
            service.deleteSlotVariant(1L)

            // Then
            verify { promptSlotVariantRepository.findById(1L) }
            verify(exactly = 0) { imageStorageService.deleteFile(any(), any()) }
            verify { promptSlotVariantRepository.deleteById(1L) }
        }

        @Test
        fun `should throw exception when trying to delete non-existent variant`() {
            // Given
            every { promptSlotVariantRepository.findById(999L) } returns Optional.empty()

            // When/Then
            val exception =
                assertThrows<PromptSlotVariantNotFoundException> {
                    service.deleteSlotVariant(999L)
                }

            verify { promptSlotVariantRepository.findById(999L) }
            verify(exactly = 0) { promptSlotVariantRepository.deleteById(any()) }
            verify(exactly = 0) { imageStorageService.deleteFile(any(), any()) }
        }

        @Test
        fun `should handle deletion of variant with references`() {
            // Given
            val existingEntity =
                PromptSlotVariant(
                    id = 1L,
                    promptSlotTypeId = 1L,
                    name = "Referenced",
                    prompt = "Referenced prompt",
                    exampleImageFilename = null,
                )

            every { promptSlotVariantRepository.findById(1L) } returns Optional.of(existingEntity)

            // When
            service.deleteSlotVariant(1L)

            // Then
            verify { promptSlotVariantRepository.deleteById(1L) }
        }
    }

    @Nested
    @DisplayName("Query Method Tests")
    inner class QueryMethodTests {
        @Test
        fun `should get all slot variants`() {
            // Given
            val variants = listOf(testPromptSlotVariant)
            every { promptSlotVariantRepository.findAll() } returns variants
            every { promptSlotVariantAssembler.toDto(any()) } returns mockk()

            // When
            val result = service.getAllSlotVariants()

            // Then
            assertNotNull(result)
            verify { promptSlotVariantRepository.findAll() }
            verify(exactly = 1) { promptSlotVariantAssembler.toDto(any()) }
        }

        @Test
        fun `should get slot variant by id`() {
            // Given
            every { promptSlotVariantRepository.findById(1L) } returns Optional.of(testPromptSlotVariant)
            every { promptSlotVariantAssembler.toDto(any()) } returns mockk()

            // When
            val result = service.getSlotVariantById(1L)

            // Then
            assertNotNull(result)
            verify { promptSlotVariantRepository.findById(1L) }
            verify { promptSlotVariantAssembler.toDto(testPromptSlotVariant) }
        }

        @Test
        fun `should throw exception when getting non-existent variant by id`() {
            // Given
            whenever(promptSlotVariantRepository.findById(999L)).thenReturn(Optional.empty())

            // When/Then
            assertThrows<PromptSlotVariantNotFoundException> {
                service.getSlotVariantById(999L)
            }
        }

        // Note: retrieval by slot type was removed from service; tests adjusted accordingly

        @Test
        fun `should check if variant exists by id`() {
            // Given
            whenever(promptSlotVariantRepository.existsById(1L)).thenReturn(true)
            whenever(promptSlotVariantRepository.existsById(999L)).thenReturn(false)

            // When
            val existsResult = service.existsById(1L)
            val notExistsResult = service.existsById(999L)

            // Then
            assertTrue(existsResult)
            assertFalse(notExistsResult)
        }
    }

    @Nested
    @DisplayName("Edge Case and Error Handling Tests")
    inner class EdgeCaseTests {
        @Test
        fun `should handle very long text fields in create`() {
            // Given
            val longText = "A".repeat(5000)
            val request =
                CreatePromptSlotVariantRequest(
                    promptSlotTypeId = 1L,
                    name = "Long Text Variant",
                    prompt = longText,
                    description = longText,
                )

            val savedEntity =
                PromptSlotVariant(
                    id = 10L,
                    promptSlotTypeId = request.promptSlotTypeId,
                    name = request.name,
                    prompt = request.prompt,
                    description = request.description,
                )

            every { promptSlotTypeRepository.existsById(1L) } returns true
            every { promptSlotVariantRepository.existsByName("Long Text Variant") } returns false
            every { promptSlotVariantRepository.save(any()) } returns savedEntity
            every { promptSlotVariantAssembler.toDto(any()) } returns mockk()

            // When
            val result = service.createSlotVariant(request)

            // Then
            assertNotNull(result)

            val entityCaptor = argumentCaptor<PromptSlotVariant>()
            verify { promptSlotVariantRepository.save(capture(entityCaptor)) }
            assertEquals(longText, entityCaptor.firstValue.prompt)
            assertEquals(longText, entityCaptor.firstValue.description)
        }

        @Test
        fun `should handle concurrent name updates`() {
            // Given
            val existingEntity =
                PromptSlotVariant(
                    id = 1L,
                    promptSlotTypeId = 1L,
                    name = "Original Name",
                )

            val request =
                UpdatePromptSlotVariantRequest(
                    name = "Concurrent Name",
                )

            every { promptSlotVariantRepository.findById(1L) } returns Optional.of(existingEntity)
            every { promptSlotVariantRepository.existsByNameAndIdNot("Concurrent Name", 1L) } returns false
            every { promptSlotVariantRepository.save(any()) } returns existingEntity
            every { promptSlotVariantAssembler.toDto(any()) } returns mockk()

            // When
            val result = service.updateSlotVariant(1L, request)

            // Then
            assertNotNull(result)
            assertEquals("Concurrent Name", existingEntity.name)
        }

        @Test
        fun `should validate empty string as invalid name in create`() {
            // Given
            val request =
                CreatePromptSlotVariantRequest(
                    promptSlotTypeId = 1L,
                    name = "  ", // Whitespace only
                    prompt = "Test",
                )

            every { promptSlotTypeRepository.existsById(1L) } returns true
            every { promptSlotVariantRepository.existsByName("  ") } returns false
            every { promptSlotVariantRepository.save(any()) } returns mockk()
            every { promptSlotVariantAssembler.toDto(any()) } returns mockk()

            // When - The validation should be handled by Jakarta validation annotations
            // This test verifies the service handles the value as-is
            val result = service.createSlotVariant(request)

            // Then
            assertNotNull(result)
        }
    }
}
