package com.jotoai.voenix.shop.domain.articles.dto

import java.time.OffsetDateTime

data class ArticleMugVariantDto(
    val id: Long,
    val articleId: Long,
    val insideColorCode: String,
    val outsideColorCode: String,
    val name: String,
    val exampleImageUrl: String?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
