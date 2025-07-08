package com.jotoai.voenix.shop.mugs.dto

import java.time.OffsetDateTime

data class MugCategoryDto(
    val id: Long,
    val name: String,
    val description: String?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
