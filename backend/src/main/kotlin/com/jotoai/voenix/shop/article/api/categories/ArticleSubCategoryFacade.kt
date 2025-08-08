package com.jotoai.voenix.shop.article.api.categories

import com.jotoai.voenix.shop.article.api.dto.categories.ArticleSubCategoryDto
import com.jotoai.voenix.shop.article.api.dto.categories.CreateArticleSubCategoryRequest
import com.jotoai.voenix.shop.article.api.dto.categories.UpdateArticleSubCategoryRequest

/**
 * Facade for ArticleSubCategory write operations.
 */
interface ArticleSubCategoryFacade {
    fun createSubCategory(request: CreateArticleSubCategoryRequest): ArticleSubCategoryDto

    fun updateSubCategory(
        id: Long,
        request: UpdateArticleSubCategoryRequest,
    ): ArticleSubCategoryDto

    fun deleteSubCategory(id: Long)
}
