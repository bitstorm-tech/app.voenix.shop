package com.jotoai.voenix.shop.article.api.categories

import com.jotoai.voenix.shop.article.api.dto.categories.ArticleCategoryDto
import com.jotoai.voenix.shop.article.api.dto.categories.CreateArticleCategoryRequest
import com.jotoai.voenix.shop.article.api.dto.categories.UpdateArticleCategoryRequest

/**
 * Facade for ArticleCategory write operations.
 */
interface ArticleCategoryFacade {
    fun createCategory(request: CreateArticleCategoryRequest): ArticleCategoryDto

    fun updateCategory(
        id: Long,
        request: UpdateArticleCategoryRequest,
    ): ArticleCategoryDto

    fun deleteCategory(id: Long)
}
