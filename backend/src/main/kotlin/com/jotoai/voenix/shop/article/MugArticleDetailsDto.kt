package com.jotoai.voenix.shop.article

import java.time.OffsetDateTime

data class MugArticleDetailsDto(
    val articleId: Long,
    val heightMm: Int,
    val diameterMm: Int,
    val printTemplateWidthMm: Int,
    val printTemplateHeightMm: Int,
    val documentFormatWidthMm: Int? = null,
    val documentFormatHeightMm: Int? = null,
    val documentFormatMarginBottomMm: Int? = null,
    val fillingQuantity: String? = null,
    val dishwasherSafe: Boolean = true,
    val createdAt: OffsetDateTime? = null,
    val updatedAt: OffsetDateTime? = null,
)
