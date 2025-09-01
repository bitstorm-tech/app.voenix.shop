package com.jotoai.voenix.shop.article.internal.web

import com.jotoai.voenix.shop.article.api.ArticleFacade
import com.jotoai.voenix.shop.article.api.ArticleQueryService
import com.jotoai.voenix.shop.article.api.dto.ArticleDto
import com.jotoai.voenix.shop.article.api.dto.ArticlePaginatedResponse
import com.jotoai.voenix.shop.article.api.dto.ArticleWithDetailsDto
import com.jotoai.voenix.shop.article.api.dto.CreateArticleRequest
import com.jotoai.voenix.shop.article.api.dto.FindArticlesQuery
import com.jotoai.voenix.shop.article.api.dto.UpdateArticleRequest
import com.jotoai.voenix.shop.article.internal.web.dto.ArticleSearchCriteria
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/articles")
@PreAuthorize("hasRole('ADMIN')")
class AdminArticleController(
    private val articleQueryService: ArticleQueryService,
    private val articleFacade: ArticleFacade,
) {
    @GetMapping
    fun findAll(
        @ModelAttribute criteria: ArticleSearchCriteria,
    ): ResponseEntity<ArticlePaginatedResponse<ArticleDto>> {
        val response =
            articleQueryService.findAll(
                FindArticlesQuery(
                    page = criteria.page,
                    size = criteria.size,
                    articleType = criteria.type,
                    categoryId = criteria.categoryId,
                    subcategoryId = criteria.subcategoryId,
                    active = criteria.active,
                ),
            )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: Long,
    ): ResponseEntity<ArticleWithDetailsDto> {
        val article = articleQueryService.findById(id)
        return ResponseEntity.ok(article)
    }

    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateArticleRequest,
    ): ResponseEntity<ArticleWithDetailsDto> {
        val article = articleFacade.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(article)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateArticleRequest,
    ): ResponseEntity<ArticleWithDetailsDto> {
        val article = articleFacade.update(id, request)
        return ResponseEntity.ok(article)
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        articleFacade.delete(id)
        return ResponseEntity.noContent().build()
    }
}
