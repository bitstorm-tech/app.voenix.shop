package com.jotoai.voenix.shop.article

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
    val active: Boolean,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
