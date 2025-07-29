package com.jotoai.voenix.shop.api.admin.articles

import com.jotoai.voenix.shop.common.dto.PaginatedResponse
import com.jotoai.voenix.shop.domain.articles.dto.ArticleDto
import com.jotoai.voenix.shop.domain.articles.dto.ArticleWithDetailsDto
import com.jotoai.voenix.shop.domain.articles.dto.CreateArticleRequest
import com.jotoai.voenix.shop.domain.articles.dto.UpdateArticleRequest
import com.jotoai.voenix.shop.domain.articles.enums.ArticleType
import com.jotoai.voenix.shop.domain.articles.service.ArticleService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/articles")
@PreAuthorize("hasRole('ADMIN')")
class ArticleController(
    private val articleService: ArticleService,
) {
    @GetMapping
    fun findAll(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false) type: ArticleType?,
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(required = false) subcategoryId: Long?,
        @RequestParam(required = false) active: Boolean?,
    ): ResponseEntity<PaginatedResponse<ArticleDto>> {
        val response =
            articleService.findAll(
                page = page,
                size = size,
                articleType = type,
                categoryId = categoryId,
                subcategoryId = subcategoryId,
                active = active,
            )
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: Long,
    ): ResponseEntity<ArticleWithDetailsDto> {
        val article = articleService.findById(id)
        return ResponseEntity.ok(article)
    }

    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateArticleRequest,
    ): ResponseEntity<ArticleWithDetailsDto> {
        val article = articleService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(article)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateArticleRequest,
    ): ResponseEntity<ArticleWithDetailsDto> {
        val article = articleService.update(id, request)
        return ResponseEntity.ok(article)
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        articleService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
