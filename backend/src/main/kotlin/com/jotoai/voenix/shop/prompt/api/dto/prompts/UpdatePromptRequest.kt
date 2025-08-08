package com.jotoai.voenix.shop.prompt.api.dto.prompts

import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.PromptSlotVariantRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Size

data class UpdatePromptRequest(
    @field:Size(max = 500, message = "Title must not exceed 500 characters")
    val title: String? = null,
    val promptText: String? = null,
    val categoryId: Long? = null,
    val subcategoryId: Long? = null,
    val active: Boolean? = null,
    val exampleImageFilename: String? = null,
    @field:Valid
    val slots: List<PromptSlotVariantRequest>? = null,
)
