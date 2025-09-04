package com.jotoai.voenix.shop.article.internal.web

import com.jotoai.voenix.shop.article.api.ArticleCategoryService
import com.jotoai.voenix.shop.article.api.dto.categories.ArticleSubCategoryDto
import com.jotoai.voenix.shop.article.api.dto.categories.CreateArticleSubCategoryRequest
import com.jotoai.voenix.shop.article.api.dto.categories.UpdateArticleSubCategoryRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/articles/subcategories")
@PreAuthorize("hasRole('ADMIN')")
class AdminArticleSubCategoryController(
    private val articleCategoryService: ArticleCategoryService,
) {
    @GetMapping
    fun getAllSubCategories(): List<ArticleSubCategoryDto> = articleCategoryService.getAllSubCategories()

    @GetMapping("/{id}")
    fun getSubCategoryById(
        @PathVariable id: Long,
    ): ArticleSubCategoryDto = articleCategoryService.getSubCategoryById(id)

    @GetMapping("/category/{categoryId}")
    fun getSubCategoriesByCategoryId(
        @PathVariable categoryId: Long,
    ): List<ArticleSubCategoryDto> = articleCategoryService.getSubCategoriesByCategoryId(categoryId)

    @PostMapping
    fun createArticleSubCategory(
        @Valid @RequestBody createArticleSubCategoryRequest: CreateArticleSubCategoryRequest,
    ): ArticleSubCategoryDto = articleCategoryService.createSubCategory(createArticleSubCategoryRequest)

    @PutMapping("/{id}")
    fun updateArticleSubCategory(
        @PathVariable id: Long,
        @Valid @RequestBody updateArticleSubCategoryRequest: UpdateArticleSubCategoryRequest,
    ): ArticleSubCategoryDto = articleCategoryService.updateSubCategory(id, updateArticleSubCategoryRequest)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteArticleSubCategory(
        @PathVariable id: Long,
    ) {
        articleCategoryService.deleteSubCategory(id)
    }
}
