package com.jotoai.voenix.shop.prompts.dto

import jakarta.validation.constraints.Size

data class UpdateSlotTypeRequest(
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String? = null
)