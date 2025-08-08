package com.jotoai.voenix.shop.prompt.api.dto.slottypes

import java.time.OffsetDateTime

data class PromptSlotTypeDto(
    val id: Long,
    val name: String,
    val position: Int,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
