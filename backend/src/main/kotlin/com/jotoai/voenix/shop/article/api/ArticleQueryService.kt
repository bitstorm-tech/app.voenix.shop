package com.jotoai.voenix.shop.article.api

import com.jotoai.voenix.shop.article.api.dto.ArticleDto
import com.jotoai.voenix.shop.article.api.dto.ArticlePaginatedResponse
import com.jotoai.voenix.shop.article.api.dto.ArticleWithDetailsDto
import com.jotoai.voenix.shop.article.api.dto.FindArticlesQuery
import com.jotoai.voenix.shop.article.api.dto.MugArticleDetailsDto
import com.jotoai.voenix.shop.article.api.dto.MugArticleVariantDto
import com.jotoai.voenix.shop.article.api.dto.PublicMugDto

/**
 * Query service for Article module read operations.
 * Transitional PR1 version referencing existing DTOs in domain.articles.
 */
interface ArticleQueryService {
    fun getArticlesByIds(ids: Collection<Long>): Map<Long, ArticleDto>

    fun getMugVariantsByIds(ids: Collection<Long>): Map<Long, MugArticleVariantDto>

    fun getCurrentGrossPrice(articleId: Long): Long

    fun validateVariantBelongsToArticle(
        articleId: Long,
        variantId: Long,
    ): Boolean

    fun getMugDetailsByArticleId(articleId: Long): MugArticleDetailsDto?

    fun findAll(query: FindArticlesQuery): ArticlePaginatedResponse<ArticleDto>

    fun findById(id: Long): ArticleWithDetailsDto

    fun findPublicMugs(): List<PublicMugDto>
}
