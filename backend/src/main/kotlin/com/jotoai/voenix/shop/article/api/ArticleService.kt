package com.jotoai.voenix.shop.article.api

import com.jotoai.voenix.shop.article.api.dto.ArticleDto
import com.jotoai.voenix.shop.article.api.dto.MugArticleDetailsDto
import com.jotoai.voenix.shop.article.api.dto.MugArticleVariantDto

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
