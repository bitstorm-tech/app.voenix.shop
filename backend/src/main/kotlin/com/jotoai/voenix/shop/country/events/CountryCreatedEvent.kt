package com.jotoai.voenix.shop.country.events

import com.jotoai.voenix.shop.country.api.dto.CountryDto
import org.springframework.modulith.events.Externalized

/**
 * Event published when a new country is created.
 */
@Externalized
data class CountryCreatedEvent(
    val country: CountryDto,
)
