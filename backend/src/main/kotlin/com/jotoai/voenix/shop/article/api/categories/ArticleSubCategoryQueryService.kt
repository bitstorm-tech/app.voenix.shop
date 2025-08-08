package com.jotoai.voenix.shop.article.api.categories

import com.jotoai.voenix.shop.domain.articles.categories.dto.ArticleSubCategoryDto

/**
 * Query service for ArticleSubCategory read operations.
 */
interface ArticleSubCategoryQueryService {
    fun getAllSubCategories(): List<ArticleSubCategoryDto>
    fun getSubCategoryById(id: Long): ArticleSubCategoryDto
    fun getSubCategoriesByCategoryId(categoryId: Long): List<ArticleSubCategoryDto>
    fun searchSubCategoriesByName(name: String): List<ArticleSubCategoryDto>
}
