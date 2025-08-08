package com.jotoai.voenix.shop.api.admin.articles.categories

import com.jotoai.voenix.shop.domain.articles.categories.dto.ArticleSubCategoryDto
import com.jotoai.voenix.shop.domain.articles.categories.dto.CreateArticleSubCategoryRequest
import com.jotoai.voenix.shop.domain.articles.categories.dto.UpdateArticleSubCategoryRequest
import com.jotoai.voenix.shop.article.api.categories.ArticleSubCategoryFacade
import com.jotoai.voenix.shop.article.api.categories.ArticleSubCategoryQueryService
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
    private val articleSubCategoryQueryService: ArticleSubCategoryQueryService,
    private val articleSubCategoryFacade: ArticleSubCategoryFacade,
) {
    @GetMapping
    fun getAllSubCategories(): List<ArticleSubCategoryDto> = articleSubCategoryQueryService.getAllSubCategories()

    @GetMapping("/{id}")
    fun getSubCategoryById(
        @PathVariable id: Long,
    ): ArticleSubCategoryDto = articleSubCategoryQueryService.getSubCategoryById(id)

    @GetMapping("/category/{categoryId}")
    fun getSubCategoriesByCategoryId(
        @PathVariable categoryId: Long,
    ): List<ArticleSubCategoryDto> = articleSubCategoryQueryService.getSubCategoriesByCategoryId(categoryId)

    @GetMapping("/search")
    fun searchSubCategoriesByName(
        @RequestParam name: String,
    ): List<ArticleSubCategoryDto> = articleSubCategoryQueryService.searchSubCategoriesByName(name)

    @PostMapping
    fun createArticleSubCategory(
        @Valid @RequestBody createArticleSubCategoryRequest: CreateArticleSubCategoryRequest,
    ): ArticleSubCategoryDto = articleSubCategoryFacade.createSubCategory(createArticleSubCategoryRequest)

    @PutMapping("/{id}")
    fun updateArticleSubCategory(
        @PathVariable id: Long,
        @Valid @RequestBody updateArticleSubCategoryRequest: UpdateArticleSubCategoryRequest,
    ): ArticleSubCategoryDto = articleSubCategoryFacade.updateSubCategory(id, updateArticleSubCategoryRequest)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteArticleSubCategory(
        @PathVariable id: Long,
    ) {
        articleSubCategoryFacade.deleteSubCategory(id)
    }
}
