package com.jotoai.voenix.shop.article.api.variants

import com.jotoai.voenix.shop.domain.articles.dto.CreateMugArticleVariantRequest
import com.jotoai.voenix.shop.domain.articles.dto.MugArticleVariantDto

/**
 * Facade for Mug variant write operations.
 */
interface MugVariantFacade {
    fun create(articleId: Long, request: CreateMugArticleVariantRequest): MugArticleVariantDto
    fun update(variantId: Long, request: CreateMugArticleVariantRequest): MugArticleVariantDto
    fun delete(variantId: Long)
    fun updateExampleImage(variantId: Long, filename: String): MugArticleVariantDto
    fun removeExampleImage(variantId: Long): MugArticleVariantDto
}
