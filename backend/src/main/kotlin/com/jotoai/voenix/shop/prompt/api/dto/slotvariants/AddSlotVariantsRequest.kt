package com.jotoai.voenix.shop.prompt.api.dto.slotvariants

import jakarta.validation.constraints.NotEmpty

data class AddSlotVariantsRequest(
    @field:NotEmpty(message = "Slot variant IDs must not be empty")
    val slotIds: List<Long>,
)
