package com.jotoai.voenix.shop.article.internal.assembler

import com.jotoai.voenix.shop.article.api.dto.ShirtArticleVariantDto
import com.jotoai.voenix.shop.article.internal.entity.ShirtArticleVariant
import com.jotoai.voenix.shop.image.api.ImageService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import org.springframework.stereotype.Component

/**
 * Assembler for converting ShirtArticleVariant entities to DTOs.
 * Replaces the toDto() method in the entity to remove ApplicationContextAware usage.
 */
@Component
class ShirtArticleVariantAssembler(
    private val imageService: ImageService,
) {
    /**
     * Converts a ShirtArticleVariant entity to its DTO representation.
     *
     * @param entity The ShirtArticleVariant entity to convert
     * @return The corresponding ShirtArticleVariantDto
     * @throws IllegalArgumentException if the ShirtArticleVariant ID is null
     */
    fun toDto(entity: ShirtArticleVariant): ShirtArticleVariantDto =
        ShirtArticleVariantDto(
            id = requireNotNull(entity.id) { "ShirtArticleVariant ID cannot be null when converting to DTO" },
            articleId = entity.article.id!!,
            color = entity.color,
            size = entity.size,
            exampleImageUrl =
                entity.exampleImageFilename?.let { filename ->
                    imageService.getUrl(filename, ImageType.SHIRT_VARIANT_EXAMPLE)
                },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
}
