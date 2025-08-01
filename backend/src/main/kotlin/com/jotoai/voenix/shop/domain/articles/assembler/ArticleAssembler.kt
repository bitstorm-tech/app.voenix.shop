package com.jotoai.voenix.shop.domain.articles.assembler

import com.jotoai.voenix.shop.domain.articles.dto.ArticleDto
import com.jotoai.voenix.shop.domain.articles.entity.Article
import com.jotoai.voenix.shop.domain.articles.enums.ArticleType
import org.springframework.stereotype.Component

/**
 * Assembler for converting Article entities to DTOs.
 * Replaces the toDto() method in the entity to remove ApplicationContextAware usage.
 */
@Component
class ArticleAssembler(
    private val mugArticleVariantAssembler: MugArticleVariantAssembler,
    private val shirtArticleVariantAssembler: ShirtArticleVariantAssembler,
) {
    /**
     * Converts an Article entity to its DTO representation.
     * Conditionally populates variants based on article type:
     * - Only populates mugVariants for MUG type
     * - Only populates shirtVariants for SHIRT type
     *
     * @param entity The Article entity to convert
     * @return The corresponding ArticleDto
     * @throws IllegalArgumentException if the Article ID is null
     */
    fun toDto(entity: Article): ArticleDto =
        ArticleDto(
            id = requireNotNull(entity.id) { "Article ID cannot be null when converting to DTO" },
            name = entity.name,
            descriptionShort = entity.descriptionShort,
            descriptionLong = entity.descriptionLong,
            active = entity.active,
            articleType = entity.articleType,
            categoryId = entity.category.id!!,
            categoryName = entity.category.name,
            subcategoryId = entity.subcategory?.id,
            subcategoryName = entity.subcategory?.name,
            supplierId = entity.supplier?.id,
            supplierName = entity.supplier?.name,
            supplierArticleName = entity.supplierArticleName,
            supplierArticleNumber = entity.supplierArticleNumber,
            mugVariants =
                when (entity.articleType) {
                    ArticleType.MUG -> entity.mugVariants.map { mugArticleVariantAssembler.toDto(it) }
                    else -> null
                },
            shirtVariants =
                when (entity.articleType) {
                    ArticleType.SHIRT -> entity.shirtVariants.map { shirtArticleVariantAssembler.toDto(it) }
                    else -> null
                },
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )
}
