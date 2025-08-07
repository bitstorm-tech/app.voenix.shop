package com.jotoai.voenix.shop.country.internal

import com.jotoai.voenix.shop.country.api.dto.CountryDto
import com.jotoai.voenix.shop.country.api.dto.CreateCountryRequest
import com.jotoai.voenix.shop.country.api.dto.UpdateCountryRequest
import com.jotoai.voenix.shop.country.api.exceptions.CountryNotFoundException
import com.jotoai.voenix.shop.country.events.CountryCreatedEvent
import com.jotoai.voenix.shop.country.events.CountryDeletedEvent
import com.jotoai.voenix.shop.country.events.CountryUpdatedEvent
import com.jotoai.voenix.shop.country.internal.entity.Country
import com.jotoai.voenix.shop.country.internal.repository.CountryRepository
import com.jotoai.voenix.shop.country.internal.service.CountryServiceImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.context.ApplicationEventPublisher
import java.time.OffsetDateTime
import java.util.Optional

class CountryServiceImplTest {
    private lateinit var countryRepository: CountryRepository
    private lateinit var eventPublisher: ApplicationEventPublisher
    private lateinit var countryService: CountryServiceImpl

    @BeforeEach
    fun setUp() {
        countryRepository = mock()
        eventPublisher = mock()
        countryService = CountryServiceImpl(countryRepository, eventPublisher)
    }

    @Test
    fun `getAllCountries should return list of countries`() {
        // Given
        val country1 =
            Country(
                id = 1L,
                name = "Germany",
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )
        val country2 =
            Country(
                id = 2L,
                name = "France",
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )
        val countries = listOf(country1, country2)

        whenever(countryRepository.findAll()).thenReturn(countries)

        // When
        val result = countryService.getAllCountries()

        // Then
        assertEquals(2, result.size)
        assertEquals("Germany", result[0].name)
        assertEquals("France", result[1].name)
        verify(countryRepository).findAll()
    }

    @Test
    fun `getCountryById should return country when found`() {
        // Given
        val countryId = 1L
        val country =
            Country(
                id = countryId,
                name = "Germany",
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )

        whenever(countryRepository.findById(1L)).thenReturn(Optional.of(country))

        // When
        val result = countryService.getCountryById(countryId)

        // Then
        assertNotNull(result)
        assertEquals("Germany", result.name)
        verify(countryRepository).findById(countryId)
    }

    @Test
    fun `getCountryById should throw exception when not found`() {
        // Given
        whenever(countryRepository.findById(999L)).thenReturn(Optional.empty())

        // When/Then
        assertThrows<CountryNotFoundException> {
            countryService.getCountryById(999L)
        }
        verify(countryRepository).findById(999L)
    }

    @Test
    fun `createCountry should save and publish event`() {
        // Given
        val request = CreateCountryRequest(name = "New Country")
        val savedCountry =
            Country(
                id = 1L,
                name = "New Country",
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )

        whenever(countryRepository.save(any())).thenReturn(savedCountry)
        whenever(countryRepository.existsByName(any())).thenReturn(false)

        // When
        val result = countryService.createCountry(request)

        // Then
        assertEquals("New Country", result.name)
        verify(countryRepository).save(any())
        verify(eventPublisher).publishEvent(
            argThat<CountryCreatedEvent> {
                this.country.id == 1L && this.country.name == "New Country"
            },
        )
    }

    @Test
    fun `updateCountry should update and publish event`() {
        // Given
        val countryId = 1L
        val existingCountry =
            Country(
                id = countryId,
                name = "Old Country",
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )
        val updatedCountry =
            Country(
                id = countryId,
                name = "Updated Country",
                createdAt = existingCountry.createdAt,
                updatedAt = OffsetDateTime.now(),
            )
        val request = UpdateCountryRequest(name = "Updated Country")

        whenever(countryRepository.existsByNameAndIdNot("Updated Country", countryId)).thenReturn(false)
        whenever(countryRepository.findById(countryId)).thenReturn(Optional.of(existingCountry))
        whenever(countryRepository.save(any())).thenReturn(updatedCountry)

        // When
        val result = countryService.updateCountry(countryId, request)

        // Then
        assertEquals("Updated Country", result.name)
        verify(countryRepository).save(any())

        val oldDto =
            CountryDto(
                id = existingCountry.id!!,
                name = existingCountry.name,
                createdAt = existingCountry.createdAt,
                updatedAt = existingCountry.updatedAt,
            )
        val newDto =
            CountryDto(
                id = updatedCountry.id!!,
                name = updatedCountry.name,
                createdAt = updatedCountry.createdAt,
                updatedAt = updatedCountry.updatedAt,
            )

        verify(eventPublisher).publishEvent(
            argThat<CountryUpdatedEvent> {
                this.oldCountry.id == oldDto.id && this.newCountry.name == "Updated Country"
            },
        )
    }

    @Test
    fun `updateCountry should throw exception when not found`() {
        // Given
        val request = UpdateCountryRequest(name = "Updated")

        whenever(countryRepository.findById(999L)).thenReturn(Optional.empty())

        // When/Then
        assertThrows<CountryNotFoundException> {
            countryService.updateCountry(999L, request)
        }
        verify(countryRepository, never()).save(any())
        verify(eventPublisher, never()).publishEvent(any())
    }

    @Test
    fun `deleteCountry should delete and publish event`() {
        // Given
        val countryId = 1L
        val country =
            Country(
                id = countryId,
                name = "To Delete",
                createdAt = OffsetDateTime.now(),
                updatedAt = OffsetDateTime.now(),
            )

        whenever(countryRepository.findById(1L)).thenReturn(Optional.of(country))
        doNothing().`when`(countryRepository).deleteById(1L)

        // When
        countryService.deleteCountry(countryId)

        // Then
        verify(countryRepository).deleteById(1L)
        verify(eventPublisher).publishEvent(
            argThat<CountryDeletedEvent> {
                this.country.id == 1L
            },
        )
    }

    @Test
    fun `deleteCountry should throw exception when not found`() {
        // Given
        whenever(countryRepository.findById(999L)).thenReturn(Optional.empty())

        // When/Then
        assertThrows<CountryNotFoundException> {
            countryService.deleteCountry(999L)
        }
        verify(countryRepository, never()).deleteById(any())
        verify(eventPublisher, never()).publishEvent(any())
    }
}
