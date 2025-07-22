package com.jotoai.voenix.shop.api.admin.articles.categories

import com.jotoai.voenix.shop.domain.articles.categories.dto.ArticleCategoryDto
import com.jotoai.voenix.shop.domain.articles.categories.dto.CreateArticleCategoryRequest
import com.jotoai.voenix.shop.domain.articles.categories.dto.UpdateArticleCategoryRequest
import com.jotoai.voenix.shop.domain.articles.categories.service.ArticleCategoryService
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
@RequestMapping("/api/admin/articles/categories")
@PreAuthorize("hasRole('ADMIN')")
class AdminArticleCategoryController(
    private val articleCategoryService: ArticleCategoryService,
) {
    @GetMapping
    fun getAllCategories(): List<ArticleCategoryDto> = articleCategoryService.getAllCategories()

    @GetMapping("/{id}")
    fun getCategoryById(
        @PathVariable id: Long,
    ): ArticleCategoryDto = articleCategoryService.getCategoryById(id)

    @GetMapping("/search")
    fun searchCategoriesByName(
        @RequestParam name: String,
    ): List<ArticleCategoryDto> = articleCategoryService.searchCategoriesByName(name)

    @PostMapping
    fun createArticleCategory(
        @Valid @RequestBody createArticleCategoryRequest: CreateArticleCategoryRequest,
    ): ArticleCategoryDto = articleCategoryService.createCategory(createArticleCategoryRequest)

    @PutMapping("/{id}")
    fun updateArticleCategory(
        @PathVariable id: Long,
        @Valid @RequestBody updateArticleCategoryRequest: UpdateArticleCategoryRequest,
    ): ArticleCategoryDto = articleCategoryService.updateCategory(id, updateArticleCategoryRequest)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteArticleCategory(
        @PathVariable id: Long,
    ) {
        articleCategoryService.deleteCategory(id)
    }
}
