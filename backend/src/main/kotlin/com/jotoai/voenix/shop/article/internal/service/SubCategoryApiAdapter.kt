package com.jotoai.voenix.shop.article.internal.service

import com.jotoai.voenix.shop.article.api.categories.ArticleSubCategoryFacade
import com.jotoai.voenix.shop.article.api.categories.ArticleSubCategoryQueryService
import com.jotoai.voenix.shop.domain.articles.categories.dto.ArticleSubCategoryDto
import com.jotoai.voenix.shop.domain.articles.categories.dto.CreateArticleSubCategoryRequest
import com.jotoai.voenix.shop.domain.articles.categories.dto.UpdateArticleSubCategoryRequest
import com.jotoai.voenix.shop.domain.articles.categories.service.ArticleSubCategoryService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SubCategoryApiAdapter(
    private val delegate: ArticleSubCategoryService,
) : ArticleSubCategoryQueryService, ArticleSubCategoryFacade {

    override fun getAllSubCategories(): List<ArticleSubCategoryDto> = delegate.getAllSubCategories()

    override fun getSubCategoryById(id: Long): ArticleSubCategoryDto = delegate.getSubCategoryById(id)

    override fun getSubCategoriesByCategoryId(categoryId: Long): List<ArticleSubCategoryDto> =
        delegate.getSubCategoriesByCategoryId(categoryId)

    override fun searchSubCategoriesByName(name: String): List<ArticleSubCategoryDto> =
        delegate.searchSubCategoriesByName(name)

    @Transactional
    override fun createSubCategory(request: CreateArticleSubCategoryRequest): ArticleSubCategoryDto =
        delegate.createSubCategory(request)

    @Transactional
    override fun updateSubCategory(
        id: Long,
        request: UpdateArticleSubCategoryRequest,
    ): ArticleSubCategoryDto = delegate.updateSubCategory(id, request)

    @Transactional
    override fun deleteSubCategory(id: Long) {
        delegate.deleteSubCategory(id)
    }
}
