package com.jotoai.voenix.shop.mugs.dto

import java.time.OffsetDateTime

data class MugVariantDto(
    val id: Long,
    val mugId: Long,
    val colorCode: String,
    val exampleImageUrl: String,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
