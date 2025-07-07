package com.jotoai.voenix.shop.prompts.dto

import java.time.OffsetDateTime

data class PromptCategoryDto(
    val id: Long,
    val name: String,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?
)