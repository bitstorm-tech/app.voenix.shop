package com.jotoai.voenix.shop.domain.articles.assembler

import com.jotoai.voenix.shop.domain.articles.dto.MugArticleVariantDto
import com.jotoai.voenix.shop.domain.articles.entity.MugArticleVariant
import com.jotoai.voenix.shop.domain.images.dto.ImageType
import com.jotoai.voenix.shop.domain.images.service.StoragePathService
import org.springframework.stereotype.Component

/**
 * Assembler for converting MugArticleVariant entities to DTOs.
 * Replaces the toDto() method in the entity to remove ApplicationContextAware usage.
 */
@Component
class MugArticleVariantAssembler(
    private val storagePathService: StoragePathService,
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
                    storagePathService.getImageUrl(ImageType.MUG_VARIANT_EXAMPLE, filename)
                },
            articleVariantNumber = entity.articleVariantNumber,
            isDefault = entity.isDefault,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
}
