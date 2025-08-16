package com.jotoai.voenix.shop.article.internal.assembler

import com.jotoai.voenix.shop.article.api.dto.MugArticleVariantSummaryDto
import com.jotoai.voenix.shop.article.api.dto.MugWithVariantsSummaryDto
import com.jotoai.voenix.shop.article.internal.entity.Article
import com.jotoai.voenix.shop.article.internal.entity.MugArticleVariant
import com.jotoai.voenix.shop.image.api.StoragePathService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import org.springframework.stereotype.Component

/**
 * Assembler for converting Article entities with variants to summary DTOs.
 */
@Component
class MugWithVariantsSummaryAssembler(
    private val storagePathService: StoragePathService,
) {
    /**
     * Converts an Article entity with mug variants to its summary DTO representation.
     *
     * @param entity The Article entity to convert
     * @return The corresponding MugWithVariantsSummaryDto
     * @throws IllegalArgumentException if the Article ID is null
     */
    fun toDto(entity: Article): MugWithVariantsSummaryDto =
        MugWithVariantsSummaryDto(
            id = requireNotNull(entity.id) { "Article ID cannot be null when converting to DTO" },
            name = entity.name,
            supplierArticleName = entity.supplierArticleName,
            variants = entity.mugVariants?.map { variantToSummaryDto(it) } ?: emptyList(),
        )

    private fun variantToSummaryDto(entity: MugArticleVariant): MugArticleVariantSummaryDto =
        MugArticleVariantSummaryDto(
            id = requireNotNull(entity.id) { "MugArticleVariant ID cannot be null when converting to DTO" },
            name = entity.name,
            insideColorCode = entity.insideColorCode,
            outsideColorCode = entity.outsideColorCode,
            articleVariantNumber = entity.articleVariantNumber,
            exampleImageUrl =
                entity.exampleImageFilename?.let { filename ->
                    storagePathService.getImageUrl(ImageType.MUG_VARIANT_EXAMPLE, filename)
                },
            active = entity.active,
        )
}
