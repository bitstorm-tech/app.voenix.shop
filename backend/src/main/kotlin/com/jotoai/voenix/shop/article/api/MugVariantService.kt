package com.jotoai.voenix.shop.article.api

import com.jotoai.voenix.shop.article.api.dto.CopyVariantsRequest
import com.jotoai.voenix.shop.article.api.dto.CreateMugArticleVariantRequest
import com.jotoai.voenix.shop.article.api.dto.MugArticleVariantDto
import com.jotoai.voenix.shop.article.api.dto.MugWithVariantsSummaryDto

/**
 * Unified service for Mug variant operations (read + write).
 */
interface MugVariantService {
    // Read operations
    fun findAllMugsWithVariants(excludeMugId: Long?): List<MugWithVariantsSummaryDto>

    // Write operations
    fun create(
        articleId: Long,
        request: CreateMugArticleVariantRequest,
    ): MugArticleVariantDto

    fun update(
        variantId: Long,
        request: CreateMugArticleVariantRequest,
    ): MugArticleVariantDto

    fun delete(variantId: Long)

    fun updateExampleImage(
        variantId: Long,
        filename: String,
    ): MugArticleVariantDto

    fun removeExampleImage(variantId: Long): MugArticleVariantDto

    fun copyVariants(
        targetMugId: Long,
        request: CopyVariantsRequest,
    ): List<MugArticleVariantDto>
}
