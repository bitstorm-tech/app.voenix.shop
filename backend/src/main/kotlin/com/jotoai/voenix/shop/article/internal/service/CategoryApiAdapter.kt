package com.jotoai.voenix.shop.article.internal.service

import com.jotoai.voenix.shop.article.api.categories.ArticleCategoryFacade
import com.jotoai.voenix.shop.article.api.categories.ArticleCategoryQueryService
import com.jotoai.voenix.shop.article.api.dto.categories.ArticleCategoryDto
import com.jotoai.voenix.shop.article.api.dto.categories.CreateArticleCategoryRequest
import com.jotoai.voenix.shop.article.api.dto.categories.UpdateArticleCategoryRequest
import com.jotoai.voenix.shop.domain.articles.categories.service.ArticleCategoryService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CategoryApiAdapter(
    private val delegate: ArticleCategoryService,
) : ArticleCategoryQueryService,
    ArticleCategoryFacade {
    override fun getAllCategories(): List<ArticleCategoryDto> = delegate.getAllCategories()

    override fun getCategoryById(id: Long): ArticleCategoryDto = delegate.getCategoryById(id)

    override fun searchCategoriesByName(name: String): List<ArticleCategoryDto> = delegate.searchCategoriesByName(name)

    @Transactional
    override fun createCategory(request: CreateArticleCategoryRequest): ArticleCategoryDto = delegate.createCategory(request)

    @Transactional
    override fun updateCategory(
        id: Long,
        request: UpdateArticleCategoryRequest,
    ): ArticleCategoryDto = delegate.updateCategory(id, request)

    @Transactional
    override fun deleteCategory(id: Long) {
        delegate.deleteCategory(id)
    }
}
