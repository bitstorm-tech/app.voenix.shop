package com.jotoai.voenix.shop.prompt

import com.jotoai.voenix.shop.prompt.internal.service.PromptSlotTypeServiceImpl
import com.jotoai.voenix.shop.prompt.internal.service.PromptSlotVariantServiceImpl
import com.jotoai.voenix.shop.prompt.internal.dto.slottypes.CreatePromptSlotTypeRequest
import com.jotoai.voenix.shop.prompt.internal.dto.slotvariants.CreatePromptSlotVariantRequest
import com.jotoai.voenix.shop.prompt.internal.dto.slotvariants.UpdatePromptSlotVariantRequest
import com.jotoai.voenix.shop.application.ResourceNotFoundException
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("PromptSlotVariant Integration Tests")
class PromptSlotVariantIntegrationTest {
    @Autowired
    private lateinit var promptSlotVariantService: PromptSlotVariantServiceImpl

    @Autowired
    private lateinit var promptSlotTypeService: PromptSlotTypeServiceImpl

    @Autowired
    private lateinit var entityManager: EntityManager

    private var testSlotTypeId: Long = 0L

    @BeforeEach
    fun setUp() {
        // Create a test slot type for use in tests
        val slotType =
            promptSlotTypeService.createPromptSlotType(
                CreatePromptSlotTypeRequest(
                    name = "Test Background Type",
                    position = 1,
                ),
            )
        testSlotTypeId = slotType.id!!

        // Clear entity manager cache to ensure fresh reads
        entityManager.flush()
        entityManager.clear()
    }

    @Nested
    @DisplayName("Create Operations")
    inner class CreateOperations {
        @Test
        fun `should create slot variant with all fields`() {
            // Given
            val request =
                CreatePromptSlotVariantRequest(
                    promptSlotTypeId = testSlotTypeId,
                    name = "Nature Background",
                    prompt = "A beautiful natural landscape with mountains",
                    description = "Perfect for nature-themed designs",
                    exampleImageFilename = "nature-bg.jpg",
                )

            // When
            val created = promptSlotVariantService.createSlotVariant(request)

            // Then
            assertNotNull(created)
            assertNotNull(created.id)
            assertEquals(testSlotTypeId, created.promptSlotTypeId)
            assertEquals("Nature Background", created.name)
            assertEquals("A beautiful natural landscape with mountains", created.prompt)
            assertEquals("Perfect for nature-themed designs", created.description)
            assertNotNull(created.exampleImageUrl) // URL should be generated from filename
            assertNotNull(created.createdAt)
            assertNotNull(created.updatedAt)

            // Verify it can be retrieved
            val retrieved = promptSlotVariantService.getSlotVariantById(created.id!!)
            assertEquals(created.id, retrieved.id)
            assertEquals(created.name, retrieved.name)
        }

        @Test
        fun `should create slot variant with minimal fields`() {
            // Given
            val request =
                CreatePromptSlotVariantRequest(
                    promptSlotTypeId = testSlotTypeId,
                    name = "Minimal Variant",
                )

            // When
            val created = promptSlotVariantService.createSlotVariant(request)

            // Then
            assertNotNull(created)
            assertEquals("Minimal Variant", created.name)
            assertNull(created.prompt)
            assertNull(created.description)
            assertNull(created.exampleImageUrl)
        }

        @Test
        fun `should fail to create with non-existent slot type`() {
            // Given
            val request =
                CreatePromptSlotVariantRequest(
                    promptSlotTypeId = 99999L,
                    name = "Invalid Type Variant",
                )

            // When/Then
            assertThrows<IllegalArgumentException> {
                promptSlotVariantService.createSlotVariant(request)
            }
        }

        @Test
        fun `should fail to create with duplicate name`() {
            // Given
            val firstRequest =
                CreatePromptSlotVariantRequest(
                    promptSlotTypeId = testSlotTypeId,
                    name = "Duplicate Name Test",
                )
            promptSlotVariantService.createSlotVariant(firstRequest)

            val secondRequest =
                CreatePromptSlotVariantRequest(
                    promptSlotTypeId = testSlotTypeId,
                    name = "Duplicate Name Test",
                )

            // When/Then
            assertThrows<IllegalArgumentException> {
                promptSlotVariantService.createSlotVariant(secondRequest)
            }
        }

        @Test
        fun `should create multiple variants for same slot type`() {
            // Given/When
            val variant1 =
                promptSlotVariantService.createSlotVariant(
                    CreatePromptSlotVariantRequest(
                        promptSlotTypeId = testSlotTypeId,
                        name = "Variant 1",
                    ),
                )

            val variant2 =
                promptSlotVariantService.createSlotVariant(
                    CreatePromptSlotVariantRequest(
                        promptSlotTypeId = testSlotTypeId,
                        name = "Variant 2",
                    ),
                )

            // Then
            val variants =
                promptSlotVariantService
                    .getAllSlotVariants()
                    .filter { it.promptSlotTypeId == testSlotTypeId }
            assertEquals(2, variants.size)
            assertTrue(variants.any { it.id == variant1.id })
            assertTrue(variants.any { it.id == variant2.id })
        }
    }

    @Nested
    @DisplayName("Update Operations")
    inner class UpdateOperations {
        @Test
        fun `should update all fields of slot variant`() {
            // Given
            val created =
                promptSlotVariantService.createSlotVariant(
                    CreatePromptSlotVariantRequest(
                        promptSlotTypeId = testSlotTypeId,
                        name = "Original Name",
                        prompt = "Original prompt",
                        description = "Original description",
                        exampleImageFilename = "original.jpg",
                    ),
                )

            // Create another slot type for testing type change
            val newSlotType =
                promptSlotTypeService.createPromptSlotType(
                    CreatePromptSlotTypeRequest(
                        name = "New Type",
                        position = 2,
                    ),
                )

            val updateRequest =
                UpdatePromptSlotVariantRequest(
                    promptSlotTypeId = newSlotType.id,
                    name = "Updated Name",
                    prompt = "Updated prompt",
                    description = "Updated description",
                    exampleImageFilename = "updated.jpg",
                )

            // When
            val updated = promptSlotVariantService.updateSlotVariant(created.id!!, updateRequest)

            // Then
            assertEquals(created.id, updated.id)
            assertEquals(newSlotType.id, updated.promptSlotTypeId)
            assertEquals("Updated Name", updated.name)
            assertEquals("Updated prompt", updated.prompt)
            assertEquals("Updated description", updated.description)
            assertEquals(created.createdAt, updated.createdAt)
            assertTrue(updated.updatedAt!! >= created.updatedAt!!)
        }

        @Test
        fun `should update only specified fields`() {
            // Given
            val created =
                promptSlotVariantService.createSlotVariant(
                    CreatePromptSlotVariantRequest(
                        promptSlotTypeId = testSlotTypeId,
                        name = "Original",
                        prompt = "Keep this",
                        description = "Keep this too",
                    ),
                )

            val updateRequest =
                UpdatePromptSlotVariantRequest(
                    name = "Only Update Name",
                )

            // When
            val updated = promptSlotVariantService.updateSlotVariant(created.id!!, updateRequest)

            // Then
            assertEquals("Only Update Name", updated.name)
            assertEquals("Keep this", updated.prompt)
            assertEquals("Keep this too", updated.description)
            assertEquals(testSlotTypeId, updated.promptSlotTypeId)
        }

        @Test
        fun `should fail to update to duplicate name`() {
            // Given
            val variant1 =
                promptSlotVariantService.createSlotVariant(
                    CreatePromptSlotVariantRequest(
                        promptSlotTypeId = testSlotTypeId,
                        name = "Existing Name",
                    ),
                )

            val variant2 =
                promptSlotVariantService.createSlotVariant(
                    CreatePromptSlotVariantRequest(
                        promptSlotTypeId = testSlotTypeId,
                        name = "To Be Updated",
                    ),
                )

            val updateRequest =
                UpdatePromptSlotVariantRequest(
                    name = "Existing Name",
                )

            // When/Then
            assertThrows<IllegalArgumentException> {
                promptSlotVariantService.updateSlotVariant(variant2.id!!, updateRequest)
            }
        }

        @Test
        fun `should fail to update non-existent variant`() {
            // Given
            val updateRequest =
                UpdatePromptSlotVariantRequest(
                    name = "New Name",
                )

            // When/Then
            assertThrows<ResourceNotFoundException> {
                promptSlotVariantService.updateSlotVariant(99999L, updateRequest)
            }
        }

        @Test
        fun `should allow updating variant to same name`() {
            // Given
            val created =
                promptSlotVariantService.createSlotVariant(
                    CreatePromptSlotVariantRequest(
                        promptSlotTypeId = testSlotTypeId,
                        name = "Same Name",
                        prompt = "Old prompt",
                    ),
                )

            val updateRequest =
                UpdatePromptSlotVariantRequest(
                    name = "Same Name",
                    prompt = "New prompt",
                )

            // When
            val updated = promptSlotVariantService.updateSlotVariant(created.id!!, updateRequest)

            // Then
            assertEquals("Same Name", updated.name)
            assertEquals("New prompt", updated.prompt)
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    inner class DeleteOperations {
        @Test
        fun `should delete existing slot variant`() {
            // Given
            val created =
                promptSlotVariantService.createSlotVariant(
                    CreatePromptSlotVariantRequest(
                        promptSlotTypeId = testSlotTypeId,
                        name = "To Be Deleted",
                    ),
                )
            val variantId = created.id!!

            // When
            promptSlotVariantService.deleteSlotVariant(variantId)

            // Then
            assertThrows<ResourceNotFoundException> {
                promptSlotVariantService.getSlotVariantById(variantId)
            }
            assertFalse(promptSlotVariantService.existsById(variantId))
        }

        @Test
        fun `should fail to delete non-existent variant`() {
            // When/Then
            assertThrows<ResourceNotFoundException> {
                promptSlotVariantService.deleteSlotVariant(99999L)
            }
        }

        @Test
        fun `should delete variant without affecting others`() {
            // Given
            val variant1 =
                promptSlotVariantService.createSlotVariant(
                    CreatePromptSlotVariantRequest(
                        promptSlotTypeId = testSlotTypeId,
                        name = "Keep This One",
                    ),
                )

            val variant2 =
                promptSlotVariantService.createSlotVariant(
                    CreatePromptSlotVariantRequest(
                        promptSlotTypeId = testSlotTypeId,
                        name = "Delete This One",
                    ),
                )

            // When
            promptSlotVariantService.deleteSlotVariant(variant2.id!!)

            // Then
            assertTrue(promptSlotVariantService.existsById(variant1.id!!))
            assertFalse(promptSlotVariantService.existsById(variant2.id!!))

            val remaining =
                promptSlotVariantService
                    .getAllSlotVariants()
                    .filter { it.promptSlotTypeId == testSlotTypeId }
            assertEquals(1, remaining.size)
            assertEquals(variant1.id, remaining[0].id)
        }
    }

    @Nested
    @DisplayName("Query Operations")
    inner class QueryOperations {
        @Test
        fun `should retrieve all slot variants`() {
            // Given
            val initialCount = promptSlotVariantService.getAllSlotVariants().size

            promptSlotVariantService.createSlotVariant(
                CreatePromptSlotVariantRequest(
                    promptSlotTypeId = testSlotTypeId,
                    name = "Variant A",
                ),
            )

            promptSlotVariantService.createSlotVariant(
                CreatePromptSlotVariantRequest(
                    promptSlotTypeId = testSlotTypeId,
                    name = "Variant B",
                ),
            )

            // When
            val allVariants = promptSlotVariantService.getAllSlotVariants()

            // Then
            assertEquals(initialCount + 2, allVariants.size)
            assertTrue(allVariants.any { it.name == "Variant A" })
            assertTrue(allVariants.any { it.name == "Variant B" })
        }

        @Test
        fun `should retrieve variants by slot type`() {
            // Given
            val anotherSlotType =
                promptSlotTypeService.createPromptSlotType(
                    CreatePromptSlotTypeRequest(
                        name = "Another Type",
                        position = 3,
                    ),
                )

            promptSlotVariantService.createSlotVariant(
                CreatePromptSlotVariantRequest(
                    promptSlotTypeId = testSlotTypeId,
                    name = "Type 1 Variant",
                ),
            )

            promptSlotVariantService.createSlotVariant(
                CreatePromptSlotVariantRequest(
                    promptSlotTypeId = anotherSlotType.id!!,
                    name = "Type 2 Variant",
                ),
            )

            // When
            val type1Variants =
                promptSlotVariantService
                    .getAllSlotVariants()
                    .filter { it.promptSlotTypeId == testSlotTypeId }
            val type2Variants =
                promptSlotVariantService
                    .getAllSlotVariants()
                    .filter { it.promptSlotTypeId == anotherSlotType.id!! }

            // Then
            assertEquals(1, type1Variants.size)
            assertEquals("Type 1 Variant", type1Variants[0].name)

            assertEquals(1, type2Variants.size)
            assertEquals("Type 2 Variant", type2Variants[0].name)
        }

        @Test
        fun `should return empty when querying variants for non-existent slot type`() {
            // When
            val results =
                promptSlotVariantService
                    .getAllSlotVariants()
                    .filter { it.promptSlotTypeId == 99999L }

            // Then
            assertTrue(results.isEmpty())
        }

        @Test
        fun `should correctly check variant existence`() {
            // Given
            val created =
                promptSlotVariantService.createSlotVariant(
                    CreatePromptSlotVariantRequest(
                        promptSlotTypeId = testSlotTypeId,
                        name = "Exists Check",
                    ),
                )

            // When/Then
            assertTrue(promptSlotVariantService.existsById(created.id!!))
            assertFalse(promptSlotVariantService.existsById(99999L))
        }
    }

    @Nested
    @DisplayName("Transaction and Rollback Scenarios")
    inner class TransactionScenarios {
        @Test
        fun `should rollback creation on exception`() {
            // Given
            val initialCount = promptSlotVariantService.getAllSlotVariants().size

            // When/Then
            assertThrows<IllegalArgumentException> {
                promptSlotVariantService.createSlotVariant(
                    CreatePromptSlotVariantRequest(
                        promptSlotTypeId = 99999L, // Non-existent
                        name = "Should Not Be Created",
                    ),
                )
            }

            // Verify no variant was created
            val finalCount = promptSlotVariantService.getAllSlotVariants().size
            assertEquals(initialCount, finalCount)

            val allVariants = promptSlotVariantService.getAllSlotVariants()
            assertFalse(allVariants.any { it.name == "Should Not Be Created" })
        }

        @Test
        fun `should maintain data consistency across operations`() {
            // Given
            val variant =
                promptSlotVariantService.createSlotVariant(
                    CreatePromptSlotVariantRequest(
                        promptSlotTypeId = testSlotTypeId,
                        name = "Consistency Test",
                        prompt = "Original",
                    ),
                )

            // When - Update
            promptSlotVariantService.updateSlotVariant(
                variant.id!!,
                UpdatePromptSlotVariantRequest(prompt = "Updated"),
            )

            // Then - Verify consistency
            val retrieved = promptSlotVariantService.getSlotVariantById(variant.id!!)
            assertEquals("Updated", retrieved.prompt)
            assertEquals("Consistency Test", retrieved.name) // Unchanged field

            // When - Delete
            promptSlotVariantService.deleteSlotVariant(variant.id!!)

            // Then - Verify deletion
            assertFalse(promptSlotVariantService.existsById(variant.id!!))
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Tests")
    inner class EdgeCases {
        @Test
        fun `should handle very long text fields`() {
            // Given
            val longText = "A".repeat(5000)
            val request =
                CreatePromptSlotVariantRequest(
                    promptSlotTypeId = testSlotTypeId,
                    name = "Long Text Test",
                    prompt = longText,
                    description = longText,
                )

            // When
            val created = promptSlotVariantService.createSlotVariant(request)

            // Then
            val retrieved = promptSlotVariantService.getSlotVariantById(created.id!!)
            assertEquals(longText, retrieved.prompt)
            assertEquals(longText, retrieved.description)
        }

        @Test
        fun `should handle special characters in names and text`() {
            // Given
            val specialName = "Test & Special #123 @Characters!"
            val specialPrompt = "Prompt with 'quotes' and \"double quotes\" and \n newlines"

            val request =
                CreatePromptSlotVariantRequest(
                    promptSlotTypeId = testSlotTypeId,
                    name = specialName,
                    prompt = specialPrompt,
                )

            // When
            val created = promptSlotVariantService.createSlotVariant(request)

            // Then
            val retrieved = promptSlotVariantService.getSlotVariantById(created.id!!)
            assertEquals(specialName, retrieved.name)
            assertEquals(specialPrompt, retrieved.prompt)
        }

        @Test
        fun `should handle maximum length filename`() {
            // Given
            val maxFilename = "f".repeat(500) // Max length is 500
            val request =
                CreatePromptSlotVariantRequest(
                    promptSlotTypeId = testSlotTypeId,
                    name = "Max Filename Test",
                    exampleImageFilename = maxFilename,
                )

            // When
            val created = promptSlotVariantService.createSlotVariant(request)

            // Then
            val retrieved = promptSlotVariantService.getSlotVariantById(created.id!!)
            // Note: exampleImageFilename is not exposed in DTO, only the URL
            assertNotNull(retrieved)
        }
    }
}
