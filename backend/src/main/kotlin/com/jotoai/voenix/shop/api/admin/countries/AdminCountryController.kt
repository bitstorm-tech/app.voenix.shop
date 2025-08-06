package com.jotoai.voenix.shop.api.admin.countries

import com.jotoai.voenix.shop.country.api.CountryFacade
import com.jotoai.voenix.shop.country.api.CountryQueryService
import com.jotoai.voenix.shop.country.api.dto.CountryDto
import com.jotoai.voenix.shop.country.api.dto.CreateCountryRequest
import com.jotoai.voenix.shop.country.api.dto.UpdateCountryRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/countries")
@PreAuthorize("hasRole('ADMIN')")
class AdminCountryController(
    private val countryFacade: CountryFacade,
    private val countryQueryService: CountryQueryService,
) {
    @GetMapping
    fun getAllCountries(): List<CountryDto> = countryQueryService.getAllCountries()

    @GetMapping("/{id}")
    fun getCountryById(
        @PathVariable id: Long,
    ): CountryDto = countryQueryService.getCountryById(id)

    @PostMapping
    fun createCountry(
        @Valid @RequestBody createCountryRequest: CreateCountryRequest,
    ): CountryDto = countryFacade.createCountry(createCountryRequest)

    @PutMapping("/{id}")
    fun updateCountry(
        @PathVariable id: Long,
        @Valid @RequestBody updateCountryRequest: UpdateCountryRequest,
    ): CountryDto = countryFacade.updateCountry(id, updateCountryRequest)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCountry(
        @PathVariable id: Long,
    ) {
        countryFacade.deleteCountry(id)
    }
}
