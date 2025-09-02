package com.jotoai.voenix.shop.prompt.api.dto.prompts

/**
 * Lightweight DTO for prompt summary information.
 * Used in batch operations where full prompt details are not needed.
 */
data class PromptSummaryDto(
    val id: Long,
    val title: String,
)