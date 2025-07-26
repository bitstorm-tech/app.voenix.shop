package com.jotoai.voenix.shop.domain.articles.dto

import java.time.OffsetDateTime

data class MugArticleDetailsDto(
    val articleId: Long,
    val heightMm: Int,
    val diameterMm: Int,
    val printTemplateWidthMm: Int,
    val printTemplateHeightMm: Int,
    val fillingQuantity: String? = null,
    val dishwasherSafe: Boolean = true,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
)
