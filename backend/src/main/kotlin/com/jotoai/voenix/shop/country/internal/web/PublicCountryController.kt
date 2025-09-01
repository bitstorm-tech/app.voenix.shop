package com.jotoai.voenix.shop.country.internal.web

import com.jotoai.voenix.shop.country.api.CountryService
import com.jotoai.voenix.shop.country.api.dto.CountryDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/countries")
class PublicCountryController(
    private val countryService: CountryService,
) {
    @GetMapping
    fun getAllCountries(): List<CountryDto> = countryService.getAllCountries()
}
