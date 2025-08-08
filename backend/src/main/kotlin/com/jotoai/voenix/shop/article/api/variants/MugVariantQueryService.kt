package com.jotoai.voenix.shop.article.api.variants

import com.jotoai.voenix.shop.article.api.dto.MugArticleVariantDto

/**
 * Query service for Mug variant read operations.
 */
interface MugVariantQueryService {
    fun findByArticleId(articleId: Long): List<MugArticleVariantDto>
}
