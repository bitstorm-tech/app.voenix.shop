package com.jotoai.voenix.shop.domain.suppliers.dto

import com.jotoai.voenix.shop.domain.countries.dto.CountryDto
import java.time.LocalDateTime

data class SupplierDto(
    val id: Long,
    val name: String?,
    val title: String?,
    val firstName: String?,
    val lastName: String?,
    val street: String?,
    val houseNumber: String?,
    val city: String?,
    val postalCode: Int?,
    val country: CountryDto?,
    val phoneNumber1: String?,
    val phoneNumber2: String?,
    val phoneNumber3: String?,
    val email: String?,
    val website: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
