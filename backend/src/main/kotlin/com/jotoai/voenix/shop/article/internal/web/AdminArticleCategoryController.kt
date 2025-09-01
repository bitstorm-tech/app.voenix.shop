package com.jotoai.voenix.shop.article.internal.web

import com.jotoai.voenix.shop.article.api.categories.ArticleCategoryFacade
import com.jotoai.voenix.shop.article.api.categories.ArticleCategoryQueryService
import com.jotoai.voenix.shop.article.api.dto.categories.ArticleCategoryDto
import com.jotoai.voenix.shop.article.api.dto.categories.CreateArticleCategoryRequest
import com.jotoai.voenix.shop.article.api.dto.categories.UpdateArticleCategoryRequest
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
@RequestMapping("/api/admin/articles/categories")
@PreAuthorize("hasRole('ADMIN')")
class AdminArticleCategoryController(
    private val articleCategoryQueryService: ArticleCategoryQueryService,
    private val articleCategoryFacade: ArticleCategoryFacade,
) {
    @GetMapping
    fun getAllCategories(): List<ArticleCategoryDto> = articleCategoryQueryService.getAllCategories()

    @GetMapping("/{id}")
    fun getCategoryById(
        @PathVariable id: Long,
    ): ArticleCategoryDto = articleCategoryQueryService.getCategoryById(id)

    @PostMapping
    fun createArticleCategory(
        @Valid @RequestBody createArticleCategoryRequest: CreateArticleCategoryRequest,
    ): ArticleCategoryDto = articleCategoryFacade.createCategory(createArticleCategoryRequest)

    @PutMapping("/{id}")
    fun updateArticleCategory(
        @PathVariable id: Long,
        @Valid @RequestBody updateArticleCategoryRequest: UpdateArticleCategoryRequest,
    ): ArticleCategoryDto = articleCategoryFacade.updateCategory(id, updateArticleCategoryRequest)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteArticleCategory(
        @PathVariable id: Long,
    ) {
        articleCategoryFacade.deleteCategory(id)
    }
}
