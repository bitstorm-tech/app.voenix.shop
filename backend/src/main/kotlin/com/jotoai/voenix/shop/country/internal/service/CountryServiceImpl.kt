package com.jotoai.voenix.shop.country.internal.service

import com.jotoai.voenix.shop.country.api.CountryFacade
import com.jotoai.voenix.shop.country.api.CountryQueryService
import com.jotoai.voenix.shop.country.api.dto.CountryDto
import com.jotoai.voenix.shop.country.api.dto.CreateCountryRequest
import com.jotoai.voenix.shop.country.api.dto.UpdateCountryRequest
import com.jotoai.voenix.shop.country.events.CountryCreatedEvent
import com.jotoai.voenix.shop.country.events.CountryDeletedEvent
import com.jotoai.voenix.shop.country.events.CountryUpdatedEvent
import com.jotoai.voenix.shop.country.internal.entity.Country
import com.jotoai.voenix.shop.country.internal.exception.CountryNotFoundException
import com.jotoai.voenix.shop.country.internal.repository.CountryRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CountryServiceImpl(
    private val countryRepository: CountryRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : CountryFacade,
    CountryQueryService {
    override fun getAllCountries(): List<CountryDto> = countryRepository.findAll().map { it.toDto() }

    override fun getCountryById(id: Long): CountryDto =
        countryRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { CountryNotFoundException("Country", "id", id) }

    override fun existsById(id: Long): Boolean = countryRepository.existsById(id)

    @Deprecated("Use getCountryById instead and refactor entity relationships")
    override fun getCountryEntityReference(id: Long): Any =
        countryRepository
            .findById(id)
            .orElseThrow { CountryNotFoundException("Country", "id", id) }

    @Transactional
    override fun createCountry(request: CreateCountryRequest): CountryDto {
        if (countryRepository.existsByName(request.name)) {
            throw IllegalArgumentException("Country with name '${request.name}' already exists")
        }

        val country =
            Country(
                name = request.name,
            )

        val savedCountry = countryRepository.save(country)
        val result = savedCountry.toDto()

        // Publish event
        eventPublisher.publishEvent(CountryCreatedEvent(result))

        return result
    }

    @Transactional
    override fun updateCountry(
        id: Long,
        request: UpdateCountryRequest,
    ): CountryDto {
        val country =
            countryRepository
                .findById(id)
                .orElseThrow { CountryNotFoundException("Country", "id", id) }

        val oldDto = country.toDto()

        request.name?.let { newName ->
            if (countryRepository.existsByNameAndIdNot(newName, id)) {
                throw IllegalArgumentException("Country with name '$newName' already exists")
            }
            country.name = newName
        }

        val updatedCountry = countryRepository.save(country)
        val result = updatedCountry.toDto()

        // Publish event
        eventPublisher.publishEvent(CountryUpdatedEvent(oldDto, result))

        return result
    }

    @Transactional
    override fun deleteCountry(id: Long) {
        val country =
            countryRepository
                .findById(id)
                .orElseThrow { CountryNotFoundException("Country", "id", id) }

        val countryDto = country.toDto()

        countryRepository.deleteById(id)

        // Publish event
        eventPublisher.publishEvent(CountryDeletedEvent(countryDto))
    }
}
