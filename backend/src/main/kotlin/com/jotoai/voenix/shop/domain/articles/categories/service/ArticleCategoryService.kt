package com.jotoai.voenix.shop.domain.articles.categories.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.articles.categories.dto.ArticleCategoryDto
import com.jotoai.voenix.shop.domain.articles.categories.dto.CreateArticleCategoryRequest
import com.jotoai.voenix.shop.domain.articles.categories.dto.UpdateArticleCategoryRequest
import com.jotoai.voenix.shop.domain.articles.categories.entity.ArticleCategory
import com.jotoai.voenix.shop.domain.articles.categories.repository.ArticleCategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ArticleCategoryService(
    private val articleCategoryRepository: ArticleCategoryRepository,
) {
    fun getAllCategories(): List<ArticleCategoryDto> = articleCategoryRepository.findAll().map { it.toDto() }

    fun getCategoryById(id: Long): ArticleCategoryDto =
        articleCategoryRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { ResourceNotFoundException("ArticleCategory", "id", id) }

    fun searchCategoriesByName(name: String): List<ArticleCategoryDto> =
        articleCategoryRepository.findByNameContainingIgnoreCase(name).map { it.toDto() }

    @Transactional
    fun createCategory(request: CreateArticleCategoryRequest): ArticleCategoryDto {
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
    fun updateCategory(
        id: Long,
        request: UpdateArticleCategoryRequest,
    ): ArticleCategoryDto {
        val category =
            articleCategoryRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("ArticleCategory", "id", id) }

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
    fun deleteCategory(id: Long) {
        if (!articleCategoryRepository.existsById(id)) {
            throw ResourceNotFoundException("ArticleCategory", "id", id)
        }
        articleCategoryRepository.deleteById(id)
    }
}
