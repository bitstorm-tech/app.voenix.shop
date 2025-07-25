package com.jotoai.voenix.shop.domain.articles.dto

import java.time.OffsetDateTime

data class ArticlePillowVariantDto(
    val id: Long,
    val articleId: Long,
    val color: String,
    val material: String,
    val exampleImageUrl: String?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
