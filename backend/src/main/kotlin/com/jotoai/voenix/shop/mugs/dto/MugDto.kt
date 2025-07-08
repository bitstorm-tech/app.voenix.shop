package com.jotoai.voenix.shop.mugs.dto

import java.time.OffsetDateTime

data class MugDto(
    val id: Long,
    val name: String,
    val descriptionLong: String,
    val descriptionShort: String,
    val image: String,
    val price: Int,
    val heightMm: Int,
    val diameterMm: Int,
    val printTemplateWidthMm: Int,
    val printTemplateHeightMm: Int,
    val fillingQuantity: String?,
    val dishwasherSafe: Boolean,
    val active: Boolean,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
