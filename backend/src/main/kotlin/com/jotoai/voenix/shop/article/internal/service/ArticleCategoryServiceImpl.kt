package com.jotoai.voenix.shop.article.internal.service

import com.jotoai.voenix.shop.article.api.dto.categories.ArticleCategoryDto
import com.jotoai.voenix.shop.article.api.dto.categories.CreateArticleCategoryRequest
import com.jotoai.voenix.shop.article.api.dto.categories.UpdateArticleCategoryRequest
import com.jotoai.voenix.shop.article.api.exception.ArticleNotFoundException
import com.jotoai.voenix.shop.article.internal.categories.entity.ArticleCategory
import com.jotoai.voenix.shop.article.internal.categories.repository.ArticleCategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ArticleCategoryServiceImpl(
    private val articleCategoryRepository: ArticleCategoryRepository,
) : com.jotoai.voenix.shop.article.api.categories.ArticleCategoryQueryService,
    com.jotoai.voenix.shop.article.api.categories.ArticleCategoryFacade {
    override fun getAllCategories(): List<ArticleCategoryDto> = articleCategoryRepository.findAll().map { it.toDto() }

    override fun getCategoryById(id: Long): ArticleCategoryDto =
        articleCategoryRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { ArticleNotFoundException("ArticleCategory not found with id: $id") }

    override fun searchCategoriesByName(name: String): List<ArticleCategoryDto> =
        articleCategoryRepository.findByNameContainingIgnoreCase(name).map { it.toDto() }

    @Transactional
    override fun createCategory(request: CreateArticleCategoryRequest): ArticleCategoryDto {
        if (articleCategoryRepository.existsByNameIgnoreCase(request.name)) {
            throw IllegalArgumentException("Category with name '${request.name}' already exists")
        }

        val category =
            ArticleCategory(
                name = request.name,
                description = request.description,
            )

        val savedCategory = articleCategoryRepository.save(category)
        return savedCategory.toDto()
    }

    @Transactional
    override fun updateCategory(
        id: Long,
        request: UpdateArticleCategoryRequest,
    ): ArticleCategoryDto {
        val category =
            articleCategoryRepository
                .findById(id)
                .orElseThrow { ArticleNotFoundException("ArticleCategory not found with id: $id") }

        request.name?.let { newName ->
            if (newName != category.name && articleCategoryRepository.existsByNameIgnoreCase(newName)) {
                throw IllegalArgumentException("Category with name '$newName' already exists")
            }
            category.name = newName
        }

        request.description?.let { category.description = it }

        val updatedCategory = articleCategoryRepository.save(category)
        return updatedCategory.toDto()
    }

    @Transactional
    override fun deleteCategory(id: Long) {
        if (!articleCategoryRepository.existsById(id)) {
            throw ArticleNotFoundException("ArticleCategory not found with id: $id")
        }
        articleCategoryRepository.deleteById(id)
    }
}
