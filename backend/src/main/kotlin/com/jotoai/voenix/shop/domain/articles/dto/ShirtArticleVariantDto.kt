package com.jotoai.voenix.shop.domain.articles.dto

import java.time.OffsetDateTime

data class ShirtArticleVariantDto(
    val id: Long,
    val articleId: Long,
    val color: String,
    val size: String,
    val exampleImageUrl: String?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
