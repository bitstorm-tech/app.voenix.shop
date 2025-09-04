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
) {
    fun getAllCategories(): List<ArticleCategoryDto> = articleCategoryRepository.findAll().map { it.toDto() }

    fun getCategoryById(id: Long): ArticleCategoryDto =
        articleCategoryRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { ArticleNotFoundException("ArticleCategory not found with id: $id") }

    @Transactional
    fun createCategory(request: CreateArticleCategoryRequest): ArticleCategoryDto {
        require(!articleCategoryRepository.existsByNameIgnoreCase(request.name)) {
            "Category with name '${request.name}' already exists"
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
                .orElseThrow { ArticleNotFoundException("ArticleCategory not found with id: $id") }

        request.name?.let { newName ->
            require(newName == category.name || !articleCategoryRepository.existsByNameIgnoreCase(newName)) {
                "Category with name '$newName' already exists"
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
            throw ArticleNotFoundException("ArticleCategory not found with id: $id")
        }
        articleCategoryRepository.deleteById(id)
    }
}
