package com.jotoai.voenix.shop.domain.prompts.dto

import java.time.OffsetDateTime

data class PromptSlotVariantDto(
    val id: Long,
    val promptSlotTypeId: Long,
    val promptSlotType: PromptSlotTypeDto? = null,
    val name: String,
    val prompt: String? = null,
    val description: String? = null,
    val exampleImageUrl: String? = null,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
