package com.jotoai.voenix.shop.prompt.internal.dto.pub

data class PublicPromptDto(
    val id: Long,
    val title: String,
    val exampleImageUrl: String?,
    val category: PublicPromptCategoryDto?,
    val subcategory: PublicPromptSubCategoryDto?,
    val slots: List<PublicPromptSlotDto> = emptyList(),
)
