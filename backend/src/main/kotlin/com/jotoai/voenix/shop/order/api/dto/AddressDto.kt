package com.jotoai.voenix.shop.order.api.dto

import com.jotoai.voenix.shop.order.internal.entity.Address
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

fun AddressDto.toEntity(): Address =
    Address(
        streetAddress1 = streetAddress1,
        streetAddress2 = streetAddress2,
        city = city,
        state = state,
        postalCode = postalCode,
        country = country,
    )

fun Address.toDto(): AddressDto =
    AddressDto(
        streetAddress1 = streetAddress1,
        streetAddress2 = streetAddress2,
        city = city,
        state = state,
        postalCode = postalCode,
        country = country,
    )
