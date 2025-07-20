package com.jotoai.voenix.shop.domain.articles.categories.dto

import java.time.OffsetDateTime

data class ArticleCategoryDto(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
