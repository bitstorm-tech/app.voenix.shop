package com.jotoai.voenix.shop.prompts.dto

import com.jotoai.voenix.shop.prompts.entity.Prompt
import java.time.OffsetDateTime

data class PromptDto(
    val id: Long,
    val title: String,
    val content: String?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?
) {
    companion object {
        fun from(prompt: Prompt): PromptDto = PromptDto(
            id = requireNotNull(prompt.id) { "Prompt ID cannot be null when converting to DTO" },
            title = prompt.title,
            content = prompt.content,
            createdAt = prompt.createdAt,
            updatedAt = prompt.updatedAt
        )
    }
}