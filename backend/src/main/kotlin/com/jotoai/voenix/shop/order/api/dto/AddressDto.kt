package com.jotoai.voenix.shop.order.api.dto

import com.jotoai.voenix.shop.order.internal.entity.Address
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
) {
    fun toEntity(): Address =
        Address(
            streetAddress1 = streetAddress1,
            streetAddress2 = streetAddress2,
            city = city,
            state = state,
            postalCode = postalCode,
            country = country,
        )

    companion object {
        fun fromEntity(address: Address): AddressDto =
            AddressDto(
                streetAddress1 = address.streetAddress1,
                streetAddress2 = address.streetAddress2,
                city = address.city,
                state = address.state,
                postalCode = address.postalCode,
                country = address.country,
            )
    }
}
