package com.jotoai.voenix.shop.domain.countries.dto

import jakarta.validation.constraints.Size

data class UpdateCountryRequest(
    @field:Size(max = 255, message = "Name must not exceed 255 characters")
    val name: String?,
)
