package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.image.api.ImageService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.prompt.api.dto.pub.PublicPromptSlotDto
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.PromptSlotVariantDto
import com.jotoai.voenix.shop.prompt.internal.entity.PromptSlotVariant
import org.springframework.stereotype.Component

/**
 * Assembler for converting PromptSlotVariant entities to DTOs.
 * Replaces the toDto() and toPublicDto() methods in the entity to remove ApplicationContextAware usage.
 */
@Component
class PromptSlotVariantAssembler(
    private val imageService: ImageService,
) {
    /**
     * Converts a PromptSlotVariant entity to its DTO representation.
     *
     * @param entity The PromptSlotVariant entity to convert
     * @return The corresponding PromptSlotVariantDto
     * @throws IllegalArgumentException if the PromptSlotVariant ID is null
     */
    fun toDto(entity: PromptSlotVariant): PromptSlotVariantDto =
        PromptSlotVariantDto(
            id = requireNotNull(entity.id) { "PromptSlotVariant ID cannot be null when converting to DTO" },
            promptSlotTypeId = entity.promptSlotTypeId,
            promptSlotType = entity.promptSlotType?.toDto(),
            name = entity.name,
            prompt = entity.prompt,
            description = entity.description,
            exampleImageUrl =
                entity.exampleImageFilename?.let { filename ->
                    imageService.getUrl(filename, ImageType.PROMPT_SLOT_VARIANT_EXAMPLE)
                },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    /**
     * Converts a PromptSlotVariant entity to its public DTO representation.
     *
     * @param entity The PromptSlotVariant entity to convert
     * @return The corresponding PublicPromptSlotDto
     * @throws IllegalArgumentException if the PromptSlotVariant ID is null
     */
    fun toPublicDto(entity: PromptSlotVariant): PublicPromptSlotDto =
        PublicPromptSlotDto(
            id = requireNotNull(entity.id) { "PromptSlotVariant ID cannot be null when converting to DTO" },
            name = entity.name,
            description = entity.description,
            exampleImageUrl =
                entity.exampleImageFilename?.let { filename ->
                    imageService.getUrl(filename, ImageType.PROMPT_SLOT_VARIANT_EXAMPLE)
                },
            slotType = entity.promptSlotType?.toPublicDto(),
        )
}
