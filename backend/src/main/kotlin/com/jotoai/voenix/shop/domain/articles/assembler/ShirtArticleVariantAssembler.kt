package com.jotoai.voenix.shop.domain.articles.assembler

import com.jotoai.voenix.shop.domain.articles.dto.ShirtArticleVariantDto
import com.jotoai.voenix.shop.domain.articles.entity.ShirtArticleVariant
import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import org.springframework.stereotype.Component

/**
 * Assembler for converting ShirtArticleVariant entities to DTOs.
 * Replaces the toDto() method in the entity to remove ApplicationContextAware usage.
 */
@Component
class ShirtArticleVariantAssembler(
    private val storagePathService: StoragePathService,
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
                    storagePathService.getImageUrl(ImageType.SHIRT_VARIANT_EXAMPLE, filename)
                },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
}
