package com.jotoai.voenix.shop.api.admin.articles

import com.jotoai.voenix.shop.domain.articles.dto.ArticleShirtVariantDto
import com.jotoai.voenix.shop.domain.articles.dto.CreateArticleShirtVariantRequest
import com.jotoai.voenix.shop.domain.articles.service.ShirtVariantService
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
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/articles/shirts")
@PreAuthorize("hasRole('ADMIN')")
class ShirtVariantController(
    private val shirtVariantService: ShirtVariantService,
) {
    @GetMapping("/{articleId}/variants")
    fun findByArticleId(
        @PathVariable articleId: Long,
    ): ResponseEntity<List<ArticleShirtVariantDto>> {
        val variants = shirtVariantService.findByArticleId(articleId)
        return ResponseEntity.ok(variants)
    }

    @PostMapping("/{articleId}/variants")
    fun create(
        @PathVariable articleId: Long,
        @Valid @RequestBody request: CreateArticleShirtVariantRequest,
    ): ResponseEntity<ArticleShirtVariantDto> {
        val variant = shirtVariantService.create(articleId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(variant)
    }

    @PutMapping("/variants/{variantId}")
    fun update(
        @PathVariable variantId: Long,
        @Valid @RequestBody request: CreateArticleShirtVariantRequest,
    ): ResponseEntity<ArticleShirtVariantDto> {
        val variant = shirtVariantService.update(variantId, request)
        return ResponseEntity.ok(variant)
    }

    @DeleteMapping("/variants/{variantId}")
    fun delete(
        @PathVariable variantId: Long,
    ): ResponseEntity<Void> {
        shirtVariantService.delete(variantId)
        return ResponseEntity.noContent().build()
    }
}
