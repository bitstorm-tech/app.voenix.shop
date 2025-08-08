package com.jotoai.voenix.shop.article.api.categories

import com.jotoai.voenix.shop.domain.articles.categories.dto.ArticleSubCategoryDto
import com.jotoai.voenix.shop.domain.articles.categories.dto.CreateArticleSubCategoryRequest
import com.jotoai.voenix.shop.domain.articles.categories.dto.UpdateArticleSubCategoryRequest

/**
 * Facade for ArticleSubCategory write operations.
 */
interface ArticleSubCategoryFacade {
    fun createSubCategory(request: CreateArticleSubCategoryRequest): ArticleSubCategoryDto
    fun updateSubCategory(id: Long, request: UpdateArticleSubCategoryRequest): ArticleSubCategoryDto
    fun deleteSubCategory(id: Long)
}
