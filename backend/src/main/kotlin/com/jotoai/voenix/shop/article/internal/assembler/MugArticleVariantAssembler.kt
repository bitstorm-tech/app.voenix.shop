package com.jotoai.voenix.shop.article.internal.assembler

import com.jotoai.voenix.shop.article.api.dto.MugArticleVariantDto
import com.jotoai.voenix.shop.article.internal.entity.MugArticleVariant
import com.jotoai.voenix.shop.image.api.ImageService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import org.springframework.stereotype.Component

/**
 * Assembler for converting MugArticleVariant entities to DTOs.
 * Replaces the toDto() method in the entity to remove ApplicationContextAware usage.
 */
@Component
class MugArticleVariantAssembler(
    private val imageService: ImageService,
) {
    /**
     * Converts a MugArticleVariant entity to its DTO representation.
     *
     * @param entity The MugArticleVariant entity to convert
     * @return The corresponding MugArticleVariantDto
     * @throws IllegalArgumentException if the MugArticleVariant ID is null
     */
    fun toDto(entity: MugArticleVariant): MugArticleVariantDto =
        MugArticleVariantDto(
            id = requireNotNull(entity.id) { "MugArticleVariant ID cannot be null when converting to DTO" },
            articleId = entity.article.id!!,
            insideColorCode = entity.insideColorCode,
            outsideColorCode = entity.outsideColorCode,
            name = entity.name,
            exampleImageUrl =
                entity.exampleImageFilename?.let { filename ->
                    imageService.getUrl(filename, ImageType.MUG_VARIANT_EXAMPLE)
                },
            articleVariantNumber = entity.articleVariantNumber,
            isDefault = entity.isDefault,
            active = entity.active,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
}
