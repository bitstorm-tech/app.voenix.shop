package com.jotoai.voenix.shop.article.internal.service
import com.jotoai.voenix.shop.article.internal.dto.ArticleCategoryDto
import com.jotoai.voenix.shop.article.internal.dto.ArticleSubCategoryDto
import com.jotoai.voenix.shop.article.internal.dto.CreateArticleCategoryRequest
import com.jotoai.voenix.shop.article.internal.dto.CreateArticleSubCategoryRequest
import com.jotoai.voenix.shop.article.internal.dto.UpdateArticleCategoryRequest
import com.jotoai.voenix.shop.article.internal.dto.UpdateArticleSubCategoryRequest
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
) {
    // Category queries
    fun getAllCategories(): List<ArticleCategoryDto> = categoryServiceImpl.getAllCategories()

    fun getCategoryById(id: Long): ArticleCategoryDto = categoryServiceImpl.getCategoryById(id)

    // Category commands
    @Transactional
    fun createCategory(request: CreateArticleCategoryRequest): ArticleCategoryDto = categoryServiceImpl.createCategory(request)

    @Transactional
    fun updateCategory(
        id: Long,
        request: UpdateArticleCategoryRequest,
    ): ArticleCategoryDto = categoryServiceImpl.updateCategory(id, request)

    @Transactional
    fun deleteCategory(id: Long) = categoryServiceImpl.deleteCategory(id)

    // SubCategory queries
    fun getAllSubCategories(): List<ArticleSubCategoryDto> = subCategoryServiceImpl.getAllSubCategories()

    fun getSubCategoryById(id: Long): ArticleSubCategoryDto = subCategoryServiceImpl.getSubCategoryById(id)

    fun getSubCategoriesByCategoryId(categoryId: Long): List<ArticleSubCategoryDto> =
        subCategoryServiceImpl.getSubCategoriesByCategoryId(categoryId)

    // SubCategory commands
    @Transactional
    fun createSubCategory(request: CreateArticleSubCategoryRequest): ArticleSubCategoryDto =
        subCategoryServiceImpl.createSubCategory(request)

    @Transactional
    fun updateSubCategory(
        id: Long,
        request: UpdateArticleSubCategoryRequest,
    ): ArticleSubCategoryDto = subCategoryServiceImpl.updateSubCategory(id, request)

    @Transactional
    fun deleteSubCategory(id: Long) = subCategoryServiceImpl.deleteSubCategory(id)
}
