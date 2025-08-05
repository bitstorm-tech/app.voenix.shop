package com.jotoai.voenix.shop.supplier.api.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.URL

data class UpdateSupplierRequest(
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String?,
    @field:Size(max = 100, message = "Title must not exceed 100 characters")
    val title: String?,
    @field:Size(max = 255, message = "First name must not exceed 255 characters")
    val firstName: String?,
    @field:Size(max = 255, message = "Last name must not exceed 255 characters")
    val lastName: String?,
    @field:Size(max = 255, message = "Street must not exceed 255 characters")
    val street: String?,
    @field:Size(max = 50, message = "House number must not exceed 50 characters")
    val houseNumber: String?,
    @field:Size(max = 255, message = "City must not exceed 255 characters")
    val city: String?,
    @field:Min(value = 1, message = "Postal code must be positive")
    val postalCode: Int?,
    val countryId: Long?,
    @field:Size(max = 50, message = "Phone number must not exceed 50 characters")
    @field:Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]+$", message = "Invalid phone number format")
    val phoneNumber1: String?,
    @field:Size(max = 50, message = "Phone number must not exceed 50 characters")
    @field:Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]+$", message = "Invalid phone number format")
    val phoneNumber2: String?,
    @field:Size(max = 50, message = "Phone number must not exceed 50 characters")
    @field:Pattern(regexp = "^[+]?[0-9\\s\\-\\(\\)]+$", message = "Invalid phone number format")
    val phoneNumber3: String?,
    @field:Size(max = 255, message = "Email must not exceed 255 characters")
    @field:Email(message = "Invalid email format")
    val email: String?,
    @field:Size(max = 500, message = "Website must not exceed 500 characters")
    @field:URL(message = "Invalid website URL format")
    val website: String?,
)
