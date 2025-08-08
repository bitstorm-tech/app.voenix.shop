package com.jotoai.voenix.shop.article.api.dto

import java.time.OffsetDateTime

data class MugArticleVariantDto(
    val id: Long,
    val articleId: Long,
    val insideColorCode: String,
    val outsideColorCode: String,
    val name: String,
    val exampleImageUrl: String?,
    val articleVariantNumber: String?,
    val isDefault: Boolean,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
