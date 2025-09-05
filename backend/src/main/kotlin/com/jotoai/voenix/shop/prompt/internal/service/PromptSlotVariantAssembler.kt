package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.image.ImageType
import com.jotoai.voenix.shop.prompt.PromptSlotTypeDto
import com.jotoai.voenix.shop.prompt.PromptSlotVariantDto
import com.jotoai.voenix.shop.prompt.internal.dto.pub.PublicPromptSlotDto
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
            id = idOrThrow(entity.id, "PromptSlotVariant"),
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
            id = idOrThrow(entity.id, "PromptSlotVariant"),
            name = entity.name,
            description = entity.description,
            exampleImageUrl =
                entity.exampleImageFilename?.let { filename ->
                    imageService.getUrl(filename, ImageType.PROMPT_SLOT_VARIANT_EXAMPLE)
                },
            slotType = entity.promptSlotType?.toPublicDto(),
        )

    /**
     * Converts a PromptSlotVariant to the module public API DTO used by PromptDto.
     */
    fun toPublicApiDto(entity: PromptSlotVariant): com.jotoai.voenix.shop.prompt.PromptSlotVariantDto =
        com.jotoai.voenix.shop.prompt.PromptSlotVariantDto(
            id = idOrThrow(entity.id, "PromptSlotVariant"),
            promptSlotTypeId = entity.promptSlotTypeId,
            promptSlotType =
                entity.promptSlotType?.let {
                    PromptSlotTypeDto(
                        id = idOrThrow(it.id, "PromptSlotType"),
                        name = it.name,
                        position = it.position,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt,
                    )
                },
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
}
