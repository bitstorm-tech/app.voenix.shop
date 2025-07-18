package com.jotoai.voenix.shop.domain.mugs.dto

import java.time.OffsetDateTime

data class MugSubCategoryDto(
    val id: Long,
    val mugCategoryId: Long,
    val name: String,
    val description: String?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
