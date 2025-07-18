package com.jotoai.voenix.shop.domain.prompts.dto

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size

data class UpdateSlotTypeRequest(
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String? = null,
    @field:Min(value = 0, message = "Position must be a non-negative integer")
    val position: Int? = null,
)
