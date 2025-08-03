package com.jotoai.voenix.shop.domain.orders.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class CreateOrderRequest(
    @field:NotBlank(message = "Customer email is required")
    @field:Email(message = "Customer email must be valid")
    val customerEmail: String,
    @field:NotBlank(message = "Customer first name is required")
    val customerFirstName: String,
    @field:NotBlank(message = "Customer last name is required")
    val customerLastName: String,
    val customerPhone: String? = null,
    @field:NotNull(message = "Shipping address is required")
    @field:Valid
    val shippingAddress: AddressDto,
    @field:Valid
    val billingAddress: AddressDto? = null,
    val useShippingAsBilling: Boolean = true,
    val notes: String? = null,
)
