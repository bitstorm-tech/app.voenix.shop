package com.jotoai.voenix.shop.prompts.dto

import java.time.OffsetDateTime

data class PromptDto(
    val id: Long,
    val title: String,
    val content: String?,
    val categoryId: Long?,
    val category: PromptCategoryDto?,
    val subcategoryId: Long?,
    val subcategory: PromptSubCategoryDto?,
    val active: Boolean,
    val slots: List<SlotDto> = emptyList(),
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
