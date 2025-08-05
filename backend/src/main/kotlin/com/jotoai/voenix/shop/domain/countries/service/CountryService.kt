package com.jotoai.voenix.shop.domain.countries.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.countries.dto.CountryDto
import com.jotoai.voenix.shop.domain.countries.dto.CreateCountryRequest
import com.jotoai.voenix.shop.domain.countries.dto.UpdateCountryRequest
import com.jotoai.voenix.shop.domain.countries.entity.Country
import com.jotoai.voenix.shop.domain.countries.repository.CountryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CountryService(
    private val countryRepository: CountryRepository,
) {
    fun getAllCountries(): List<CountryDto> = countryRepository.findAll().map { it.toDto() }

    fun getCountryById(id: Long): CountryDto =
        countryRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { ResourceNotFoundException("Country", "id", id) }

    @Transactional
    fun createCountry(request: CreateCountryRequest): CountryDto {
        if (countryRepository.existsByName(request.name)) {
            throw IllegalArgumentException("Country with name '${request.name}' already exists")
        }

        val country =
            Country(
                name = request.name,
            )

        val savedCountry = countryRepository.save(country)
        return savedCountry.toDto()
    }

    @Transactional
    fun updateCountry(
        id: Long,
        request: UpdateCountryRequest,
    ): CountryDto {
        val country =
            countryRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("Country", "id", id) }

        request.name?.let { newName ->
            if (countryRepository.existsByNameAndIdNot(newName, id)) {
                throw IllegalArgumentException("Country with name '$newName' already exists")
            }
            country.name = newName
        }

        val updatedCountry = countryRepository.save(country)
        return updatedCountry.toDto()
    }

    @Transactional
    fun deleteCountry(id: Long) {
        if (!countryRepository.existsById(id)) {
            throw ResourceNotFoundException("Country", "id", id)
        }
        countryRepository.deleteById(id)
    }

    /**
     * Retrieves a country entity reference by its ID for legacy entity relationships.
     * This method is intended for backward compatibility with existing JPA entities
     * that have direct foreign key relationships to Country.
     *
     * @param id The country ID
     * @return The country entity reference
     * @throws ResourceNotFoundException if the country is not found
     * @deprecated This method should be removed once all entity relationships are refactored
     */
    @Deprecated("Use getCountryById instead and refactor entity relationships")
    fun getCountryEntityReference(id: Long): Country =
        countryRepository
            .findById(id)
            .orElseThrow { ResourceNotFoundException("Country", "id", id) }
}
