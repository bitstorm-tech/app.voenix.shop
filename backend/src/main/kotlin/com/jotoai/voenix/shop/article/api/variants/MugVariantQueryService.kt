package com.jotoai.voenix.shop.article.api.variants

import com.jotoai.voenix.shop.article.api.dto.MugWithVariantsSummaryDto

/**
 * Query service for Mug variant read operations.
 */
interface MugVariantQueryService {
    fun findAllMugsWithVariants(excludeMugId: Long?): List<MugWithVariantsSummaryDto>
}
