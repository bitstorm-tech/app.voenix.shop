package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.prompt.PromptSubCategoryDto
import com.jotoai.voenix.shop.prompt.internal.dto.pub.PublicPromptSubCategoryDto
import com.jotoai.voenix.shop.prompt.internal.entity.PromptSubCategory
import com.jotoai.voenix.shop.prompt.internal.repository.PromptRepository
import org.springframework.stereotype.Component

/**
 * Assembler for converting PromptSubCategory entities to DTOs.
 */
@Component
class PromptSubCategoryAssembler(
    private val promptRepository: PromptRepository,
) {
    /**
     * Converts a PromptSubCategory entity to its DTO representation.
     *
     * @param entity The PromptSubCategory entity to convert
     * @return The corresponding PromptSubCategoryDto
     * @throws IllegalArgumentException if the PromptSubCategory or PromptCategory ID is null
     */
    fun toDto(entity: PromptSubCategory): PromptSubCategoryDto =
        PromptSubCategoryDto(
            id = idOrThrow(entity.id, "PromptSubCategory"),
            promptCategoryId = idOrThrow(entity.promptCategory.id, "PromptCategory"),
            name = entity.name,
            description = entity.description,
            promptsCount = promptRepository.countBySubcategoryId(idOrThrow(entity.id, "PromptSubCategory")),
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    /**
     * Converts a PromptSubCategory entity to its public DTO representation.
     *
     * @param entity The PromptSubCategory entity to convert
     * @return The corresponding PublicPromptSubCategoryDto
     * @throws IllegalArgumentException if the PromptSubCategory ID is null
     */
    fun toPublicDto(entity: PromptSubCategory): PublicPromptSubCategoryDto =
        PublicPromptSubCategoryDto(
            id = idOrThrow(entity.id, "PromptSubCategory"),
            name = entity.name,
            description = entity.description,
        )
}
