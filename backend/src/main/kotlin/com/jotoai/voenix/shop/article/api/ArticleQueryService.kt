package com.jotoai.voenix.shop.article.api

import com.jotoai.voenix.shop.common.dto.PaginatedResponse
import com.jotoai.voenix.shop.domain.articles.dto.ArticleDto
import com.jotoai.voenix.shop.domain.articles.dto.ArticleWithDetailsDto
import com.jotoai.voenix.shop.domain.articles.dto.PublicMugDto
import com.jotoai.voenix.shop.domain.articles.enums.ArticleType

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
