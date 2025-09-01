package com.jotoai.voenix.shop.order

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateOrderRequest(
    @field:NotBlank
    @field:Email
    val customerEmail: String,
    @field:NotBlank
    val customerFirstName: String,
    @field:NotBlank
    val customerLastName: String,
    val customerPhone: String = "",
    @field:NotNull
    @field:Valid
    val shippingAddress: AddressDto,
    @field:Valid
    val billingAddress: AddressDto? = null,
    val notes: String = "",
)
