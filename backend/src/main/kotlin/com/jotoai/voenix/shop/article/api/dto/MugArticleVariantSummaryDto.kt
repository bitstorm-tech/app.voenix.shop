package com.jotoai.voenix.shop.article.api.dto

data class MugArticleVariantSummaryDto(
    val id: Long,
    val name: String,
    val insideColorCode: String,
    val outsideColorCode: String,
    val articleVariantNumber: String?,
    val exampleImageUrl: String?,
    val active: Boolean,
)
