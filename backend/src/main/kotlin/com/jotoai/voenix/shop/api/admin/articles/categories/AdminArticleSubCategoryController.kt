package com.jotoai.voenix.shop.api.admin.articles.categories

import com.jotoai.voenix.shop.domain.articles.categories.dto.ArticleSubCategoryDto
import com.jotoai.voenix.shop.domain.articles.categories.dto.CreateArticleSubCategoryRequest
import com.jotoai.voenix.shop.domain.articles.categories.dto.UpdateArticleSubCategoryRequest
import com.jotoai.voenix.shop.domain.articles.categories.service.ArticleSubCategoryService
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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/articles/subcategories")
@PreAuthorize("hasRole('ADMIN')")
class AdminArticleSubCategoryController(
    private val articleSubCategoryService: ArticleSubCategoryService,
) {
    @GetMapping
    fun getAllSubCategories(): List<ArticleSubCategoryDto> = articleSubCategoryService.getAllSubCategories()

    @GetMapping("/{id}")
    fun getSubCategoryById(
        @PathVariable id: Long,
    ): ArticleSubCategoryDto = articleSubCategoryService.getSubCategoryById(id)

    @GetMapping("/category/{categoryId}")
    fun getSubCategoriesByCategoryId(
        @PathVariable categoryId: Long,
    ): List<ArticleSubCategoryDto> = articleSubCategoryService.getSubCategoriesByCategoryId(categoryId)

    @GetMapping("/search")
    fun searchSubCategoriesByName(
        @RequestParam name: String,
    ): List<ArticleSubCategoryDto> = articleSubCategoryService.searchSubCategoriesByName(name)

    @PostMapping
    fun createArticleSubCategory(
        @Valid @RequestBody createArticleSubCategoryRequest: CreateArticleSubCategoryRequest,
    ): ArticleSubCategoryDto = articleSubCategoryService.createSubCategory(createArticleSubCategoryRequest)

    @PutMapping("/{id}")
    fun updateArticleSubCategory(
        @PathVariable id: Long,
        @Valid @RequestBody updateArticleSubCategoryRequest: UpdateArticleSubCategoryRequest,
    ): ArticleSubCategoryDto = articleSubCategoryService.updateSubCategory(id, updateArticleSubCategoryRequest)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteArticleSubCategory(
        @PathVariable id: Long,
    ) {
        articleSubCategoryService.deleteSubCategory(id)
    }
}
