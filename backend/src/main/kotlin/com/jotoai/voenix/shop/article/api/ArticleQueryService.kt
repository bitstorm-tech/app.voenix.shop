package com.jotoai.voenix.shop.article.api

import com.jotoai.voenix.shop.article.api.dto.ArticleDto
import com.jotoai.voenix.shop.article.api.dto.ArticleWithDetailsDto
import com.jotoai.voenix.shop.article.api.dto.PublicMugDto
import com.jotoai.voenix.shop.article.api.enums.ArticleType
import com.jotoai.voenix.shop.common.dto.PaginatedResponse

/**
 * Query service for Article module read operations.
 * Transitional PR1 version referencing existing DTOs in domain.articles.
 */
interface ArticleQueryService {
    fun findAll(
        page: Int,
        size: Int,
        articleType: ArticleType? = null,
        categoryId: Long? = null,
        subcategoryId: Long? = null,
        active: Boolean? = null,
    ): PaginatedResponse<ArticleDto>

    fun findById(id: Long): ArticleWithDetailsDto

    fun findPublicMugs(): List<PublicMugDto>
}
