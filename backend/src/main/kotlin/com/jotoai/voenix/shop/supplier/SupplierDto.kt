package com.jotoai.voenix.shop.supplier

import java.time.OffsetDateTime

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
    val countryId: Long?,
    val countryName: String?,
    val phoneNumber1: String?,
    val phoneNumber2: String?,
    val phoneNumber3: String?,
    val email: String?,
    val website: String?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
