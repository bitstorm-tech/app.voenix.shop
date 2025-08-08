package com.jotoai.voenix.shop.prompt.api.dto.prompts

import com.jotoai.voenix.shop.prompt.api.dto.categories.PromptCategoryDto
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.PromptSlotVariantDto
import com.jotoai.voenix.shop.prompt.api.dto.subcategories.PromptSubCategoryDto
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
    val slots: List<PromptSlotVariantDto> = emptyList(),
    val exampleImageUrl: String?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
