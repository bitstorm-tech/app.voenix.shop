package com.jotoai.voenix.shop.prompts.dto

import jakarta.validation.constraints.Size

data class UpdatePromptRequest(
    @field:Size(max = 500, message = "Title must not exceed 500 characters")
    val title: String? = null,
    val content: String? = null,
    val categoryId: Long? = null,
    val active: Boolean? = null,
)
