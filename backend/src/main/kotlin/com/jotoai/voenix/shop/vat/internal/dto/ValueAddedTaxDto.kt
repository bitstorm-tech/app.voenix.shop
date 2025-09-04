package com.jotoai.voenix.shop.vat.internal.dto

import java.time.OffsetDateTime

data class ValueAddedTaxDto(
    val id: Long,
    val name: String,
    val percent: Int,
    val description: String?,
    val isDefault: Boolean = false,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
)
