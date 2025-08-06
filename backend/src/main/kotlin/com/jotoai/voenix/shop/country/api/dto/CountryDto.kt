package com.jotoai.voenix.shop.country.api.dto

import java.time.OffsetDateTime

data class CountryDto(
    val id: Long,
    val name: String,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
