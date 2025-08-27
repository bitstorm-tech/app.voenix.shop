package com.jotoai.voenix.shop.article.web

import com.jotoai.voenix.shop.article.api.dto.CreateShirtArticleVariantRequest
import com.jotoai.voenix.shop.article.api.dto.ShirtArticleVariantDto
import com.jotoai.voenix.shop.article.api.variants.ShirtVariantFacade
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/articles/shirts")
@PreAuthorize("hasRole('ADMIN')")
class AdminShirtVariantController(
    private val shirtVariantFacade: ShirtVariantFacade,
) {

    @PostMapping("/{articleId}/variants")
    fun create(
        @PathVariable articleId: Long,
        @Valid @RequestBody request: CreateShirtArticleVariantRequest,
    ): ResponseEntity<ShirtArticleVariantDto> {
        val variant = shirtVariantFacade.create(articleId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(variant)
    }

    @PutMapping("/variants/{variantId}")
    fun update(
        @PathVariable variantId: Long,
        @Valid @RequestBody request: CreateShirtArticleVariantRequest,
    ): ResponseEntity<ShirtArticleVariantDto> {
        val variant = shirtVariantFacade.update(variantId, request)
        return ResponseEntity.ok(variant)
    }

    @DeleteMapping("/variants/{variantId}")
    fun delete(
        @PathVariable variantId: Long,
    ): ResponseEntity<Void> {
        shirtVariantFacade.delete(variantId)
        return ResponseEntity.noContent().build()
    }
}
