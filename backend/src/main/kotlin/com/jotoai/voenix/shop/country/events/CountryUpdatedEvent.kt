package com.jotoai.voenix.shop.country.events

import com.jotoai.voenix.shop.country.api.dto.CountryDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a country is updated.
 */
@Externalized
data class CountryUpdatedEvent(
    val oldCountry: CountryDto,
    val newCountry: CountryDto,
)
