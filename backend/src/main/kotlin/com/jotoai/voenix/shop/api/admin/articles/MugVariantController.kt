package com.jotoai.voenix.shop.api.admin.articles

import com.jotoai.voenix.shop.domain.articles.dto.ArticleMugVariantDto
import com.jotoai.voenix.shop.domain.articles.dto.CreateArticleMugVariantRequest
import com.jotoai.voenix.shop.domain.articles.service.MugVariantService
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
@RequestMapping("/api/admin/articles/mugs")
@PreAuthorize("hasRole('ADMIN')")
class MugVariantController(
    private val mugVariantService: MugVariantService,
) {
    @GetMapping("/{articleId}/variants")
    fun findByArticleId(
        @PathVariable articleId: Long,
    ): ResponseEntity<List<ArticleMugVariantDto>> {
        val variants = mugVariantService.findByArticleId(articleId)
        return ResponseEntity.ok(variants)
    }

    @PostMapping("/{articleId}/variants")
    fun create(
        @PathVariable articleId: Long,
        @Valid @RequestBody request: CreateArticleMugVariantRequest,
    ): ResponseEntity<ArticleMugVariantDto> {
        val variant = mugVariantService.create(articleId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(variant)
    }

    @PutMapping("/variants/{variantId}")
    fun update(
        @PathVariable variantId: Long,
        @Valid @RequestBody request: CreateArticleMugVariantRequest,
    ): ResponseEntity<ArticleMugVariantDto> {
        val variant = mugVariantService.update(variantId, request)
        return ResponseEntity.ok(variant)
    }

    @DeleteMapping("/variants/{variantId}")
    fun delete(
        @PathVariable variantId: Long,
    ): ResponseEntity<Void> {
        mugVariantService.delete(variantId)
        return ResponseEntity.noContent().build()
    }
}
