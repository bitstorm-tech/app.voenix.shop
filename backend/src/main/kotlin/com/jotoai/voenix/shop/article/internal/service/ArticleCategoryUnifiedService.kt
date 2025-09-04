package com.jotoai.voenix.shop.article.internal.service

import com.jotoai.voenix.shop.article.api.ArticleCategoryService
import com.jotoai.voenix.shop.article.api.dto.categories.ArticleCategoryDto
import com.jotoai.voenix.shop.article.api.dto.categories.ArticleSubCategoryDto
import com.jotoai.voenix.shop.article.api.dto.categories.CreateArticleCategoryRequest
import com.jotoai.voenix.shop.article.api.dto.categories.CreateArticleSubCategoryRequest
import com.jotoai.voenix.shop.article.api.dto.categories.UpdateArticleCategoryRequest
import com.jotoai.voenix.shop.article.api.dto.categories.UpdateArticleSubCategoryRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Adapter that unifies category and subcategory operations under a single service.
 * Delegates to existing concrete services.
 */
@Service
@Transactional(readOnly = true)
class ArticleCategoryUnifiedService(
    private val categoryServiceImpl: ArticleCategoryServiceImpl,
    private val subCategoryServiceImpl: ArticleSubCategoryServiceImpl,
) : ArticleCategoryService {
    // Category queries
    override fun getAllCategories(): List<ArticleCategoryDto> = categoryServiceImpl.getAllCategories()

    override fun getCategoryById(id: Long): ArticleCategoryDto = categoryServiceImpl.getCategoryById(id)

    // Category commands
    @Transactional
    override fun createCategory(request: CreateArticleCategoryRequest): ArticleCategoryDto = categoryServiceImpl.createCategory(request)

    @Transactional
    override fun updateCategory(
        id: Long,
        request: UpdateArticleCategoryRequest,
    ): ArticleCategoryDto = categoryServiceImpl.updateCategory(id, request)

    @Transactional
    override fun deleteCategory(id: Long) = categoryServiceImpl.deleteCategory(id)

    // SubCategory queries
    override fun getAllSubCategories(): List<ArticleSubCategoryDto> = subCategoryServiceImpl.getAllSubCategories()

    override fun getSubCategoryById(id: Long): ArticleSubCategoryDto = subCategoryServiceImpl.getSubCategoryById(id)

    override fun getSubCategoriesByCategoryId(categoryId: Long): List<ArticleSubCategoryDto> =
        subCategoryServiceImpl.getSubCategoriesByCategoryId(categoryId)

    // SubCategory commands
    @Transactional
    override fun createSubCategory(request: CreateArticleSubCategoryRequest): ArticleSubCategoryDto =
        subCategoryServiceImpl.createSubCategory(request)

    @Transactional
    override fun updateSubCategory(
        id: Long,
        request: UpdateArticleSubCategoryRequest,
    ): ArticleSubCategoryDto = subCategoryServiceImpl.updateSubCategory(id, request)

    @Transactional
    override fun deleteSubCategory(id: Long) = subCategoryServiceImpl.deleteSubCategory(id)
}
