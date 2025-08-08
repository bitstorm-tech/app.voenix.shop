package com.jotoai.voenix.shop.article.api.categories

import com.jotoai.voenix.shop.domain.articles.categories.dto.ArticleCategoryDto

/**
 * Query service for ArticleCategory read operations.
 */
interface ArticleCategoryQueryService {
    fun getAllCategories(): List<ArticleCategoryDto>
    fun getCategoryById(id: Long): ArticleCategoryDto
    fun searchCategoriesByName(name: String): List<ArticleCategoryDto>
}
