package com.jotoai.voenix.shop.article.internal.dto

data class MugWithVariantsSummaryDto(
    val id: Long,
    val name: String,
    val supplierArticleName: String?,
    val variants: List<MugArticleVariantSummaryDto>,
)
