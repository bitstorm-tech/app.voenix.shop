package com.jotoai.voenix.shop.domain.prompts.assembler

import com.jotoai.voenix.shop.domain.prompts.dto.PromptDto
import com.jotoai.voenix.shop.domain.prompts.dto.PublicPromptDto
import com.jotoai.voenix.shop.domain.prompts.entity.Prompt
import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import org.springframework.stereotype.Component

/**
 * Assembler for converting Prompt entities to DTOs.
 * Replaces the toDto() and toPublicDto() methods in the entity to remove ApplicationContextAware usage.
 */
@Component
class PromptAssembler(
    private val promptSlotVariantAssembler: PromptSlotVariantAssembler,
    private val storagePathService: StoragePathService,
) {
    /**
     * Converts a Prompt entity to its DTO representation.
     *
     * @param entity The Prompt entity to convert
     * @return The corresponding PromptDto
     * @throws IllegalArgumentException if the Prompt ID is null
     */
    fun toDto(entity: Prompt): PromptDto =
        PromptDto(
            id = requireNotNull(entity.id) { "Prompt ID cannot be null when converting to DTO" },
            title = entity.title,
            promptText = entity.promptText,
            categoryId = entity.categoryId,
            category = entity.category?.toDto(),
            subcategoryId = entity.subcategoryId,
            subcategory = entity.subcategory?.toDto(),
            active = entity.active,
            slots =
                entity.promptSlotVariantMappings.map {
                    promptSlotVariantAssembler.toDto(it.promptSlotVariant)
                },
            exampleImageUrl =
                entity.exampleImageFilename?.let { filename ->
                    storagePathService.getImageUrl(ImageType.PROMPT_EXAMPLE, filename)
                },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    /**
     * Converts a Prompt entity to its public DTO representation.
     *
     * @param entity The Prompt entity to convert
     * @return The corresponding PublicPromptDto
     * @throws IllegalArgumentException if the Prompt ID is null
     */
    fun toPublicDto(entity: Prompt): PublicPromptDto =
        PublicPromptDto(
            id = requireNotNull(entity.id) { "Prompt ID cannot be null when converting to DTO" },
            title = entity.title,
            exampleImageUrl =
                entity.exampleImageFilename?.let { filename ->
                    storagePathService.getImageUrl(ImageType.PROMPT_EXAMPLE, filename)
                },
            category = entity.category?.toPublicDto(),
            subcategory = entity.subcategory?.toPublicDto(),
            slots =
                entity.promptSlotVariantMappings.map {
                    promptSlotVariantAssembler.toPublicDto(it.promptSlotVariant)
                },
        )
}
