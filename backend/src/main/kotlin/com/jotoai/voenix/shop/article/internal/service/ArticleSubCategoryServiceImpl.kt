package com.jotoai.voenix.shop.article.internal.service
import com.jotoai.voenix.shop.article.api.dto.categories.ArticleSubCategoryDto
import com.jotoai.voenix.shop.article.api.dto.categories.CreateArticleSubCategoryRequest
import com.jotoai.voenix.shop.article.api.dto.categories.UpdateArticleSubCategoryRequest
import com.jotoai.voenix.shop.article.api.exception.ArticleNotFoundException
import com.jotoai.voenix.shop.article.internal.categories.entity.ArticleSubCategory
import com.jotoai.voenix.shop.article.internal.categories.repository.ArticleCategoryRepository
import com.jotoai.voenix.shop.article.internal.categories.repository.ArticleSubCategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ArticleSubCategoryServiceImpl(
    private val articleSubCategoryRepository: ArticleSubCategoryRepository,
    private val articleCategoryRepository: ArticleCategoryRepository,
) {
    fun getAllSubCategories(): List<ArticleSubCategoryDto> =
        articleSubCategoryRepository
            .findAll()
            .map { it.toDto() }

    fun getSubCategoryById(id: Long): ArticleSubCategoryDto =
        articleSubCategoryRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { ArticleNotFoundException("ArticleSubCategory not found with id: $id") }

    fun getSubCategoriesByCategoryId(categoryId: Long): List<ArticleSubCategoryDto> =
        articleSubCategoryRepository.findByArticleCategoryId(categoryId).map { it.toDto() }

    @Transactional
    fun createSubCategory(request: CreateArticleSubCategoryRequest): ArticleSubCategoryDto {
        val articleCategory =
            articleCategoryRepository
                .findById(request.articleCategoryId)
                .orElseThrow {
                    ArticleNotFoundException(
                        "ArticleCategory not found with id: ${request.articleCategoryId}",
                    )
                }

        require(
            !articleSubCategoryRepository.existsByArticleCategoryIdAndNameIgnoreCase(
                request.articleCategoryId,
                request.name,
            ),
        ) {
            "Subcategory with name '${request.name}' already exists in this category"
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
                .orElseThrow { ArticleNotFoundException("ArticleSubCategory not found with id: $id") }

        request.articleCategoryId?.let { newCategoryId ->
            val newCategory =
                articleCategoryRepository
                    .findById(newCategoryId)
                    .orElseThrow { ArticleNotFoundException("ArticleCategory not found with id: $newCategoryId") }
            subCategory.articleCategory = newCategory
        }

        request.name?.let { newName ->
            val categoryId = subCategory.articleCategory.id!!
            require(
                newName == subCategory.name ||
                    !articleSubCategoryRepository.existsByArticleCategoryIdAndNameIgnoreCase(categoryId, newName),
            ) {
                "Subcategory with name '$newName' already exists in this category"
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
            throw ArticleNotFoundException("ArticleSubCategory not found with id: $id")
        }
        articleSubCategoryRepository.deleteById(id)
    }
}
