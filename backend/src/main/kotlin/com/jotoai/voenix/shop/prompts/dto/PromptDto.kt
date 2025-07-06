package com.jotoai.voenix.shop.prompts.dto

import java.time.LocalDateTime

data class PromptDto(
    val id: Long,
    val title: String,
    val content: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)