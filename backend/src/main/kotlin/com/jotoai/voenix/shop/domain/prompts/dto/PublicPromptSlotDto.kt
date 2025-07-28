package com.jotoai.voenix.shop.domain.prompts.dto

data class PublicPromptSlotDto(
    val id: Long,
    val name: String,
    val description: String?,
    val exampleImageUrl: String?,
    val slotType: PublicPromptSlotTypeDto?,
)
