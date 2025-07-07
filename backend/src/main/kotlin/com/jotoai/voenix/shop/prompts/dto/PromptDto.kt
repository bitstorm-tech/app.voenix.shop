package com.jotoai.voenix.shop.prompts.dto

import java.time.OffsetDateTime

data class PromptDto(
    val id: Long,
    val title: String,
    val content: String?,
    val categoryId: Long?,
    val category: PromptCategoryDto?,
    val active: Boolean,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?
)