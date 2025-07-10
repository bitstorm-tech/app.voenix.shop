package com.jotoai.voenix.shop.prompts.dto

import java.time.OffsetDateTime

data class PromptSubCategoryDto(
    val id: Long,
    val promptCategoryId: Long,
    val name: String,
    val description: String?,
    val promptsCount: Int = 0,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
