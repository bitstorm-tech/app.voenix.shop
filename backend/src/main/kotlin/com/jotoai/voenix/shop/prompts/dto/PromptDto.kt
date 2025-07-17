package com.jotoai.voenix.shop.prompts.dto

import java.time.OffsetDateTime

data class PromptDto(
    val id: Long,
    val title: String,
    val promptText: String?,
    val categoryId: Long?,
    val category: PromptCategoryDto?,
    val subcategoryId: Long?,
    val subcategory: PromptSubCategoryDto?,
    val active: Boolean,
    val slots: List<SlotDto> = emptyList(),
    val exampleImageUrl: String?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
