package com.jotoai.voenix.shop.article.api.categories

import com.jotoai.voenix.shop.article.api.dto.categories.ArticleCategoryDto

/**
 * Query service for ArticleCategory read operations.
 */
interface ArticleCategoryQueryService {
    fun getAllCategories(): List<ArticleCategoryDto>

    fun getCategoryById(id: Long): ArticleCategoryDto
}
