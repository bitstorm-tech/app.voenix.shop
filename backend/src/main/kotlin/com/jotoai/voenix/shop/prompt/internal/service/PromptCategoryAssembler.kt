package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.prompt.api.dto.categories.PromptCategoryDto
import com.jotoai.voenix.shop.prompt.api.dto.pub.PublicPromptCategoryDto
import com.jotoai.voenix.shop.prompt.internal.entity.PromptCategory
import com.jotoai.voenix.shop.prompt.internal.repository.PromptRepository
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSubCategoryRepository
import org.springframework.stereotype.Component

/**
 * Assembler for converting PromptCategory entities to DTOs.
 */
@Component
class PromptCategoryAssembler(
    private val promptRepository: PromptRepository,
    private val promptSubCategoryRepository: PromptSubCategoryRepository,
) {
    /**
     * Converts a PromptCategory entity to its DTO representation.
     *
     * @param entity The PromptCategory entity to convert
     * @return The corresponding PromptCategoryDto
     * @throws IllegalArgumentException if the PromptCategory ID is null
     */
    fun toDto(entity: PromptCategory): PromptCategoryDto =
        PromptCategoryDto(
            id = requireNotNull(entity.id) { "PromptCategory ID cannot be null when converting to DTO" },
            name = entity.name,
            promptsCount = promptRepository.countByCategoryId(entity.id),
            subcategoriesCount = promptSubCategoryRepository.countByPromptCategoryId(entity.id).toInt(),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    /**
     * Converts a PromptCategory entity to its public DTO representation.
     *
     * @param entity The PromptCategory entity to convert
     * @return The corresponding PublicPromptCategoryDto
     * @throws IllegalArgumentException if the PromptCategory ID is null
     */
    fun toPublicDto(entity: PromptCategory): PublicPromptCategoryDto =
        PublicPromptCategoryDto(
            id = requireNotNull(entity.id) { "PromptCategory ID cannot be null when converting to DTO" },
            name = entity.name,
        )
}
