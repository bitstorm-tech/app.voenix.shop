package com.jotoai.voenix.shop.domain.prompts.dto

import java.time.OffsetDateTime

data class PromptSlotTypeDto(
    val id: Long,
    val name: String,
    val position: Int,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
