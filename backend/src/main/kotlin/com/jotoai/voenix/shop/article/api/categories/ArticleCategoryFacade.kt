package com.jotoai.voenix.shop.article.api.categories

import com.jotoai.voenix.shop.domain.articles.categories.dto.ArticleCategoryDto
import com.jotoai.voenix.shop.domain.articles.categories.dto.CreateArticleCategoryRequest
import com.jotoai.voenix.shop.domain.articles.categories.dto.UpdateArticleCategoryRequest

/**
 * Facade for ArticleCategory write operations.
 */
interface ArticleCategoryFacade {
    fun createCategory(request: CreateArticleCategoryRequest): ArticleCategoryDto
    fun updateCategory(id: Long, request: UpdateArticleCategoryRequest): ArticleCategoryDto
    fun deleteCategory(id: Long)
}
