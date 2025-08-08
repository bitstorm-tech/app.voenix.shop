package com.jotoai.voenix.shop.article.api.variants

import com.jotoai.voenix.shop.domain.articles.dto.CreateShirtArticleVariantRequest
import com.jotoai.voenix.shop.domain.articles.dto.ShirtArticleVariantDto

/**
 * Facade for Shirt variant write operations.
 */
interface ShirtVariantFacade {
    fun create(articleId: Long, request: CreateShirtArticleVariantRequest): ShirtArticleVariantDto
    fun update(variantId: Long, request: CreateShirtArticleVariantRequest): ShirtArticleVariantDto
    fun delete(variantId: Long)
    fun updateExampleImage(variantId: Long, filename: String): ShirtArticleVariantDto
}
