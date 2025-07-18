package com.jotoai.voenix.shop.domain.prompts.dto

import jakarta.validation.constraints.NotEmpty

data class AddSlotsRequest(
    @field:NotEmpty(message = "Slot IDs must not be empty")
    val slotIds: List<Long>,
)
