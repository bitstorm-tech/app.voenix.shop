package com.jotoai.voenix.shop.prompt.internal.dto.categories

import java.time.OffsetDateTime

data class PromptCategoryDto(
    val id: Long,
    val name: String,
    val promptsCount: Int = 0,
    val subcategoriesCount: Int = 0,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
