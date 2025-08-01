package com.jotoai.voenix.shop.domain.articles.categories.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.articles.categories.dto.ArticleSubCategoryDto
import com.jotoai.voenix.shop.domain.articles.categories.dto.CreateArticleSubCategoryRequest
import com.jotoai.voenix.shop.domain.articles.categories.dto.UpdateArticleSubCategoryRequest
import com.jotoai.voenix.shop.domain.articles.categories.entity.ArticleSubCategory
import com.jotoai.voenix.shop.domain.articles.categories.repository.ArticleCategoryRepository
import com.jotoai.voenix.shop.domain.articles.categories.repository.ArticleSubCategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ArticleSubCategoryService(
    private val articleSubCategoryRepository: ArticleSubCategoryRepository,
    private val articleCategoryRepository: ArticleCategoryRepository,
) {
    fun getAllSubCategories(): List<ArticleSubCategoryDto> = articleSubCategoryRepository.findAll().map { it.toDto() }

    fun getSubCategoryById(id: Long): ArticleSubCategoryDto =
        articleSubCategoryRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { ResourceNotFoundException("ArticleSubCategory", "id", id) }

    fun getSubCategoriesByCategoryId(categoryId: Long): List<ArticleSubCategoryDto> =
        articleSubCategoryRepository.findByArticleCategoryId(categoryId).map { it.toDto() }

    fun searchSubCategoriesByName(name: String): List<ArticleSubCategoryDto> =
        articleSubCategoryRepository.findByNameContainingIgnoreCase(name).map { it.toDto() }

    fun searchSubCategoriesByCategoryAndName(
        categoryId: Long,
        name: String,
    ): List<ArticleSubCategoryDto> =
        articleSubCategoryRepository.findByArticleCategoryIdAndNameContainingIgnoreCase(categoryId, name).map { it.toDto() }

    @Transactional
    fun createSubCategory(request: CreateArticleSubCategoryRequest): ArticleSubCategoryDto {
        val articleCategory =
            articleCategoryRepository
                .findById(request.articleCategoryId)
                .orElseThrow { ResourceNotFoundException("ArticleCategory", "id", request.articleCategoryId) }

        if (articleSubCategoryRepository.existsByArticleCategoryIdAndNameIgnoreCase(request.articleCategoryId, request.name)) {
            throw IllegalArgumentException("Subcategory with name '${request.name}' already exists in this category")
        }

        val subCategory =
            ArticleSubCategory(
                articleCategory = articleCategory,
                name = request.name,
                description = request.description,
            )

        val savedSubCategory = articleSubCategoryRepository.save(subCategory)
        return savedSubCategory.toDto()
    }

    @Transactional
    fun updateSubCategory(
        id: Long,
        request: UpdateArticleSubCategoryRequest,
    ): ArticleSubCategoryDto {
        val subCategory =
            articleSubCategoryRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("ArticleSubCategory", "id", id) }

        request.articleCategoryId?.let { newCategoryId ->
            val newCategory =
                articleCategoryRepository
                    .findById(newCategoryId)
                    .orElseThrow { ResourceNotFoundException("ArticleCategory", "id", newCategoryId) }
            subCategory.articleCategory = newCategory
        }

        request.name?.let { newName ->
            val categoryId = subCategory.articleCategory.id!!
            if (newName != subCategory.name &&
                articleSubCategoryRepository.existsByArticleCategoryIdAndNameIgnoreCase(categoryId, newName)
            ) {
                throw IllegalArgumentException("Subcategory with name '$newName' already exists in this category")
            }
            subCategory.name = newName
        }

        request.description?.let { subCategory.description = it }

        val updatedSubCategory = articleSubCategoryRepository.save(subCategory)
        return updatedSubCategory.toDto()
    }

    @Transactional
    fun deleteSubCategory(id: Long) {
        if (!articleSubCategoryRepository.existsById(id)) {
            throw ResourceNotFoundException("ArticleSubCategory", "id", id)
        }
        articleSubCategoryRepository.deleteById(id)
    }
}
