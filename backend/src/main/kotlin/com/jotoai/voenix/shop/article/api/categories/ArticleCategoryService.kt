package com.jotoai.voenix.shop.article.api.categories

import com.jotoai.voenix.shop.article.api.dto.categories.ArticleCategoryDto
import com.jotoai.voenix.shop.article.api.dto.categories.ArticleSubCategoryDto
import com.jotoai.voenix.shop.article.api.dto.categories.CreateArticleCategoryRequest
import com.jotoai.voenix.shop.article.api.dto.categories.CreateArticleSubCategoryRequest
import com.jotoai.voenix.shop.article.api.dto.categories.UpdateArticleCategoryRequest
import com.jotoai.voenix.shop.article.api.dto.categories.UpdateArticleSubCategoryRequest

/**
 * Unified service for Article Category and SubCategory operations (read + write).
 */
interface ArticleCategoryService {
    // Category queries
    fun getAllCategories(): List<ArticleCategoryDto>

    fun getCategoryById(id: Long): ArticleCategoryDto

    // Category commands
    fun createCategory(request: CreateArticleCategoryRequest): ArticleCategoryDto

    fun updateCategory(
        id: Long,
        request: UpdateArticleCategoryRequest,
    ): ArticleCategoryDto

    fun deleteCategory(id: Long)

    // SubCategory queries
    fun getAllSubCategories(): List<ArticleSubCategoryDto>

    fun getSubCategoryById(id: Long): ArticleSubCategoryDto

    fun getSubCategoriesByCategoryId(categoryId: Long): List<ArticleSubCategoryDto>

    // SubCategory commands
    fun createSubCategory(request: CreateArticleSubCategoryRequest): ArticleSubCategoryDto

    fun updateSubCategory(
        id: Long,
        request: UpdateArticleSubCategoryRequest,
    ): ArticleSubCategoryDto

    fun deleteSubCategory(id: Long)
}
