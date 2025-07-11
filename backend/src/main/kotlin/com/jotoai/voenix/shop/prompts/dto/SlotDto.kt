package com.jotoai.voenix.shop.prompts.dto

import java.time.OffsetDateTime

data class SlotDto(
    val id: Long,
    val slotTypeId: Long,
    val slotType: SlotTypeDto? = null,
    val name: String,
    val prompt: String,
    val description: String? = null,
    val exampleImageUrl: String? = null,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
