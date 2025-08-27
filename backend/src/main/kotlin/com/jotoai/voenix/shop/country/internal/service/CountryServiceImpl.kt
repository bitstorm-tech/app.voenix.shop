package com.jotoai.voenix.shop.country.internal.service

import com.jotoai.voenix.shop.country.api.CountryService
import com.jotoai.voenix.shop.country.api.dto.CountryDto
import com.jotoai.voenix.shop.country.api.exceptions.CountryNotFoundException
import com.jotoai.voenix.shop.country.internal.repository.CountryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CountryServiceImpl(
    private val countryRepository: CountryRepository,
) : CountryService {
    override fun getAllCountries(): List<CountryDto> = countryRepository.findAll().map { it.toDto() }

    override fun getCountryById(id: Long): CountryDto =
        countryRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { CountryNotFoundException("Country", "id", id) }

    override fun existsById(id: Long): Boolean = countryRepository.existsById(id)

    
}
