package com.jotoai.voenix.shop.vat.api.dto

import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class UpdateValueAddedTaxRequest(
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String?,
    @field:Positive(message = "Percent must be positive")
    val percent: Int?,
    val description: String?,
    val isDefault: Boolean?,
)
