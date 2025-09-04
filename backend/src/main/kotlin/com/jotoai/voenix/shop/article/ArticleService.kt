package com.jotoai.voenix.shop.article

/**
 * Public article API used by other modules.
 * Exposes only read/validation operations required by non-article modules.
 */
interface ArticleService {
    fun getArticlesByIds(ids: Collection<Long>): Map<Long, ArticleDto>

    fun getMugVariantsByIds(ids: Collection<Long>): Map<Long, MugArticleVariantDto>

    fun getCurrentGrossPrice(articleId: Long): Long

    fun validateVariantBelongsToArticle(
        articleId: Long,
        variantId: Long,
    ): Boolean

    fun getMugDetailsByArticleId(articleId: Long): MugArticleDetailsDto?
}
