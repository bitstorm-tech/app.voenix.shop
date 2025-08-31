package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.image.api.ImageService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.prompt.api.dto.categories.PromptCategoryDto
import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto
import com.jotoai.voenix.shop.prompt.api.dto.pub.PublicPromptCategoryDto
import com.jotoai.voenix.shop.prompt.api.dto.pub.PublicPromptDto
import com.jotoai.voenix.shop.prompt.api.dto.pub.PublicPromptSubCategoryDto
import com.jotoai.voenix.shop.prompt.api.dto.subcategories.PromptSubCategoryDto
import com.jotoai.voenix.shop.prompt.internal.entity.Prompt
import org.springframework.stereotype.Component

/**
 * Assembler for converting Prompt entities to DTOs.
 * Replaces the toDto() and toPublicDto() methods in the entity to remove ApplicationContextAware usage.
 */
@Component
class PromptAssembler(
    private val promptSlotVariantAssembler: PromptSlotVariantAssembler,
    private val imageService: ImageService,
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
            category =
                entity.category?.let {
                    PromptCategoryDto(
                        id = requireNotNull(it.id) { "PromptCategory ID cannot be null when converting to DTO" },
                        name = it.name,
                        promptsCount = 0,
                        subcategoriesCount = 0,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                    )
                },
            subcategoryId = entity.subcategoryId,
            subcategory =
                entity.subcategory?.let {
                    PromptSubCategoryDto(
                        id = requireNotNull(it.id) { "PromptSubCategory ID cannot be null when converting to DTO" },
                        promptCategoryId =
                            requireNotNull(
                                it.promptCategory.id,
                            ) { "PromptCategory ID cannot be null when converting to DTO" },
                        name = it.name,
                        description = it.description,
                        promptsCount = 0,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                    )
                },
            active = entity.active,
            slots =
                entity.promptSlotVariantMappings.map {
                    promptSlotVariantAssembler.toDto(it.promptSlotVariant)
                },
            exampleImageUrl =
                entity.exampleImageFilename?.let { filename ->
                    imageService.getUrl(filename, ImageType.PROMPT_EXAMPLE)
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
                    imageService.getUrl(filename, ImageType.PROMPT_EXAMPLE)
                },
            category =
                entity.category?.let {
                    PublicPromptCategoryDto(
                        id = requireNotNull(it.id) { "PromptCategory ID cannot be null when converting to DTO" },
                        name = it.name,
                    )
                },
            subcategory =
                entity.subcategory?.let {
                    PublicPromptSubCategoryDto(
                        id = requireNotNull(it.id) { "PromptSubCategory ID cannot be null when converting to DTO" },
                        name = it.name,
                        description = it.description,
                    )
                },
            slots =
                entity.promptSlotVariantMappings.map {
                    promptSlotVariantAssembler.toPublicDto(it.promptSlotVariant)
                },
        )
}
