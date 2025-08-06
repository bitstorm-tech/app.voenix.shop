package com.jotoai.voenix.shop.country.events

import com.jotoai.voenix.shop.country.api.dto.CountryDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a country is deleted.
 */
@Externalized
data class CountryDeletedEvent(
    val country: CountryDto,
)
