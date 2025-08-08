package com.jotoai.voenix.shop.article.api.dto

import java.time.OffsetDateTime

data class PublicMugVariantDto(
    val id: Long,
    val mugId: Long,
    val colorCode: String, // Using outsideColorCode as primary color
    val exampleImageUrl: String?,
    val articleVariantNumber: String?,
    val isDefault: Boolean,
    val exampleImageFilename: String?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
