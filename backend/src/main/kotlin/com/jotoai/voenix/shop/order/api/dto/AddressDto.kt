package com.jotoai.voenix.shop.order.api.dto

import jakarta.validation.constraints.NotBlank

data class AddressDto(
    @field:NotBlank(message = "Street address is required")
    val streetAddress1: String,
    val streetAddress2: String? = null,
    @field:NotBlank(message = "City is required")
    val city: String,
    @field:NotBlank(message = "State is required")
    val state: String,
    @field:NotBlank(message = "Postal code is required")
    val postalCode: String,
    @field:NotBlank(message = "Country is required")
    val country: String,
)
