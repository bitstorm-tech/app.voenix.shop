package com.jotoai.voenix.shop.prompt.internal.dto.pub

data class PublicPromptSlotDto(
    val id: Long,
    val name: String,
    val description: String?,
    val exampleImageUrl: String?,
    val slotType: PublicPromptSlotTypeDto?,
)
