package com.jotoai.voenix.shop.api.admin.countries

import com.jotoai.voenix.shop.country.api.CountryService
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
    private val countryService: CountryService,
) {
    @GetMapping
    fun getAllCountries(): List<CountryDto> = countryService.getAllCountries()

    @GetMapping("/{id}")
    fun getCountryById(
        @PathVariable id: Long,
    ): CountryDto = countryService.getCountryById(id)

    @PostMapping
    fun createCountry(
        @Valid @RequestBody createCountryRequest: CreateCountryRequest,
    ): CountryDto = countryService.createCountry(createCountryRequest)

    @PutMapping("/{id}")
    fun updateCountry(
        @PathVariable id: Long,
        @Valid @RequestBody updateCountryRequest: UpdateCountryRequest,
    ): CountryDto = countryService.updateCountry(id, updateCountryRequest)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCountry(
        @PathVariable id: Long,
    ) {
        countryService.deleteCountry(id)
    }
}
