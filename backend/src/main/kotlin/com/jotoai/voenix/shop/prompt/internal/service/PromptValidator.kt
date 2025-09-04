package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.application.ResourceNotFoundException
import com.jotoai.voenix.shop.prompt.internal.repository.PromptCategoryRepository
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSlotVariantRepository
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSubCategoryRepository
import org.springframework.stereotype.Component

/**
 * Validator component for cross-entity validation in the prompt module.
 * Provides centralized validation logic for prompt-related entities and their relationships.
 */
@Component
class PromptValidator(
    private val promptCategoryRepository: PromptCategoryRepository,
    private val promptSubCategoryRepository: PromptSubCategoryRepository,
    private val promptSlotVariantRepository: PromptSlotVariantRepository,
) {
    /**
     * Validates that a category exists if provided.
     *
     * @param categoryId The category ID to validate (nullable)
     * @throws ResourceNotFoundException if the category ID is provided but doesn't exist
     */
    fun validateCategoryExists(categoryId: Long?) {
        if (categoryId != null && !promptCategoryRepository.existsById(categoryId)) {
            throw ResourceNotFoundException("PromptCategory", "id", categoryId)
        }
    }

    /**
     * Validates that a subcategory exists if provided.
     *
     * @param subcategoryId The subcategory ID to validate (nullable)
     * @throws ResourceNotFoundException if the subcategory ID is provided but doesn't exist
     */
    fun validateSubcategoryExists(subcategoryId: Long?) {
        if (subcategoryId != null && !promptSubCategoryRepository.existsById(subcategoryId)) {
            throw ResourceNotFoundException("PromptSubCategory", "id", subcategoryId)
        }
    }

    /**
     * Validates that a subcategory belongs to the specified category.
     * This method performs the validation only if both IDs are provided.
     *
     * @param categoryId The expected category ID (nullable)
     * @param subcategoryId The subcategory ID to validate (nullable)
     * @throws IllegalArgumentException if the subcategory doesn't belong to the specified category
     * @throws ResourceNotFoundException if the subcategory doesn't exist
     */
    fun validateSubcategoryBelongsToCategory(
        categoryId: Long?,
        subcategoryId: Long?,
    ) {
        // Only validate if both IDs are provided
        if (categoryId != null && subcategoryId != null) {
            val subcategory =
                promptSubCategoryRepository
                    .findById(subcategoryId)
                    .orElseThrow { ResourceNotFoundException("PromptSubCategory", "id", subcategoryId) }

            require(subcategory.promptCategory.id == categoryId) {
                "Subcategory with id $subcategoryId does not belong to category with id $categoryId"
            }
        }
    }

    /**
     * Validates that all provided slot variant IDs exist.
     *
     * @param slotVariantIds The list of slot variant IDs to validate
     * @throws ResourceNotFoundException if any slot variant ID doesn't exist
     */
    fun validateSlotVariantsExist(slotVariantIds: List<Long>) {
        if (slotVariantIds.isEmpty()) return

        val existingSlotVariants = promptSlotVariantRepository.findAllById(slotVariantIds)
        val existingIds = existingSlotVariants.mapNotNull { it.id }.toSet()
        val missingIds = slotVariantIds.toSet() - existingIds

        if (missingIds.isNotEmpty()) {
            throw ResourceNotFoundException("PromptSlotVariant", "ids", missingIds.joinToString(", "))
        }
    }

    /**
     * Validates all aspects of a prompt request in one method call.
     * This is a convenience method for common validation scenarios.
     *
     * @param categoryId The category ID to validate (nullable)
     * @param subcategoryId The subcategory ID to validate (nullable)
     * @param slotVariantIds The list of slot variant IDs to validate (defaults to empty list)
     */
    fun validatePromptRequest(
        categoryId: Long?,
        subcategoryId: Long?,
        slotVariantIds: List<Long> = emptyList(),
    ) {
        validateCategoryExists(categoryId)
        validateSubcategoryExists(subcategoryId)
        validateSubcategoryBelongsToCategory(categoryId, subcategoryId)
        validateSlotVariantsExist(slotVariantIds)
    }
}
