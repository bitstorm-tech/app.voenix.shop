package com.jotoai.voenix.shop.domain.suppliers.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min

data class UpdateSupplierRequest(
    val name: String?,
    val title: String?,
    val firstName: String?,
    val lastName: String?,
    val street: String?,
    val houseNumber: String?,
    val city: String?,
    @field:Min(value = 1, message = "Postal code must be positive")
    val postalCode: Int?,
    val countryId: Long?,
    val phoneNumber1: String?,
    val phoneNumber2: String?,
    val phoneNumber3: String?,
    @field:Email(message = "Invalid email format")
    val email: String?,
    val website: String?,
)
