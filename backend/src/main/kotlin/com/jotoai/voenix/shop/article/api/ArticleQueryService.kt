package com.jotoai.voenix.shop.article.api

import com.jotoai.voenix.shop.article.api.dto.ArticleDto
import com.jotoai.voenix.shop.article.api.dto.ArticlePaginatedResponse
import com.jotoai.voenix.shop.article.api.dto.ArticleWithDetailsDto
import com.jotoai.voenix.shop.article.api.dto.PublicMugDto
import com.jotoai.voenix.shop.article.api.enums.ArticleType

/**
 * Query service for Article module read operations.
 * Transitional PR1 version referencing existing DTOs in domain.articles.
 */
interface ArticleQueryService {
    fun getArticlesByIds(ids: Collection<Long>): Map<Long, ArticleDto>

    fun getMugVariantsByIds(ids: Collection<Long>): Map<Long, com.jotoai.voenix.shop.article.api.dto.MugArticleVariantDto>

    fun getCurrentGrossPrice(articleId: Long): Long

    fun validateVariantBelongsToArticle(
        articleId: Long,
        variantId: Long,
    ): Boolean

    fun getMugDetailsByArticleId(articleId: Long): com.jotoai.voenix.shop.article.api.dto.MugArticleDetailsDto?

    fun findAll(
        page: Int,
        size: Int,
        articleType: ArticleType? = null,
        categoryId: Long? = null,
        subcategoryId: Long? = null,
        active: Boolean? = null,
    ): ArticlePaginatedResponse<ArticleDto>

    fun findById(id: Long): ArticleWithDetailsDto

    fun findPublicMugs(): List<PublicMugDto>
}
