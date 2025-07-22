package com.jotoai.voenix.shop.domain.articles.dto

import java.time.OffsetDateTime

data class ArticlePillowDetailsDto(
    val articleId: Long,
    val widthCm: Int,
    val heightCm: Int,
    val depthCm: Int,
    val material: String,
    val fillingType: String,
    val coverRemovable: Boolean = true,
    val washable: Boolean = true,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
)
