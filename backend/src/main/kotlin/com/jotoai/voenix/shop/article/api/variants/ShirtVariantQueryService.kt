package com.jotoai.voenix.shop.article.api.variants

import com.jotoai.voenix.shop.article.api.dto.ShirtArticleVariantDto

/**
 * Query service for Shirt variant read operations.
 */
interface ShirtVariantQueryService {
    fun findByArticleId(articleId: Long): List<ShirtArticleVariantDto>
}
