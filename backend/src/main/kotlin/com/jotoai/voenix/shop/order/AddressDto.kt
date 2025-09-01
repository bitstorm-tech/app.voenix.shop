package com.jotoai.voenix.shop.order

import jakarta.validation.constraints.NotBlank

data class AddressDto(
    @field:NotBlank
    val streetAddress1: String,
    val streetAddress2: String? = null,
    @field:NotBlank
    val city: String,
    @field:NotBlank
    val state: String,
    @field:NotBlank
    val postalCode: String,
    @field:NotBlank
    val country: String,
)
