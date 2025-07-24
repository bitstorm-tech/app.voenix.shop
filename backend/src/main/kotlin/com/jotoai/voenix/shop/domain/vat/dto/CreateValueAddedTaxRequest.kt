package com.jotoai.voenix.shop.domain.vat.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size

data class CreateValueAddedTaxRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String,
    @field:NotNull(message = "Percent is required")
    @field:Positive(message = "Percent must be positive")
    val percent: Int,
    val description: String? = null,
)
