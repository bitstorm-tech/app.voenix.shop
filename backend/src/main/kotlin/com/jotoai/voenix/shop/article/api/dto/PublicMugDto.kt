package com.jotoai.voenix.shop.article.api.dto

data class PublicMugDto(
    val id: Long,
    val name: String,
    val price: Double,
    val image: String?,
    val fillingQuantity: String?,
    val descriptionShort: String?,
    val descriptionLong: String?,
    val heightMm: Int,
    val diameterMm: Int,
    val printTemplateWidthMm: Int,
    val printTemplateHeightMm: Int,
    val dishwasherSafe: Boolean,
    val variants: List<PublicMugVariantDto> = emptyList(),
)
