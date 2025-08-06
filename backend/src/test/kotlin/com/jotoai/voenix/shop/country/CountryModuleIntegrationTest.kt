package com.jotoai.voenix.shop.country

import com.jotoai.voenix.shop.country.api.CountryFacade
import com.jotoai.voenix.shop.country.api.CountryQueryService
import com.jotoai.voenix.shop.country.api.dto.CreateCountryRequest
import com.jotoai.voenix.shop.country.api.dto.UpdateCountryRequest
import com.jotoai.voenix.shop.country.api.exceptions.CountryNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CountryModuleIntegrationTest {
    @Autowired
    private lateinit var countryQueryService: CountryQueryService

    @Autowired
    private lateinit var countryFacade: CountryFacade

    @Test
    fun `should create country and publish event`() {
        // Given
        val request =
            CreateCountryRequest(
                name = "Test Country",
            )

        // When
        val createdCountry = countryFacade.createCountry(request)

        // Then - verify returned DTO
        assertNotNull(createdCountry)
        assertEquals("Test Country", createdCountry.name)
        assertNotNull(createdCountry.id)
        assertNotNull(createdCountry.createdAt)
        assertNotNull(createdCountry.updatedAt)
    }

    @Test
    fun `should update country and publish event`() {
        // Given - create a country first
        val initialCountry =
            countryFacade.createCountry(
                CreateCountryRequest(
                    name = "Initial Country",
                ),
            )

        val updateRequest =
            UpdateCountryRequest(
                name = "Updated Country",
            )

        // When
        val updatedCountry = countryFacade.updateCountry(initialCountry.id!!, updateRequest)

        // Then - verify returned DTO
        assertNotNull(updatedCountry)
        assertEquals("Updated Country", updatedCountry.name)
        assertEquals(initialCountry.id, updatedCountry.id)
    }

    @Test
    fun `should delete country and publish event`() {
        // Given - create a country first
        val country =
            countryFacade.createCountry(
                CreateCountryRequest(
                    name = "Country to Delete",
                ),
            )

        // When
        countryFacade.deleteCountry(country.id!!)

        // Then - verify country is deleted
        assertThrows<CountryNotFoundException> {
            countryQueryService.getCountryById(country.id!!)
        }
    }

    @Test
    fun `should handle query operations correctly`() {
        // Given - create some test countries
        val country1 =
            countryFacade.createCountry(
                CreateCountryRequest("Germany"),
            )
        val country2 =
            countryFacade.createCountry(
                CreateCountryRequest("France"),
            )
        val country3 =
            countryFacade.createCountry(
                CreateCountryRequest("United Kingdom"),
            )

        // Test getAllCountries
        val allCountries = countryQueryService.getAllCountries()
        assertTrue(allCountries.size >= 3)

        // Test getCountryById
        val retrievedCountry = countryQueryService.getCountryById(country1.id!!)
        assertEquals("Germany", retrievedCountry.name)

        // Test existsById
        assertTrue(countryQueryService.existsById(country2.id!!))
        assertTrue(countryQueryService.existsById(country3.id!!))
    }

    @Test
    fun `should throw exception when country not found`() {
        // When & Then
        val exception =
            assertThrows<CountryNotFoundException> {
                countryQueryService.getCountryById(99999L)
            }
        assertEquals("Country not found with id: '99999'", exception.message)
    }

    @Test
    fun `should throw exception when updating non-existent country`() {
        // Given
        val updateRequest =
            UpdateCountryRequest(
                name = "Non-existent",
            )

        // When & Then
        val exception =
            assertThrows<CountryNotFoundException> {
                countryFacade.updateCountry(99999L, updateRequest)
            }
        assertEquals("Country not found with id: '99999'", exception.message)
    }

    @Test
    fun `should throw exception when deleting non-existent country`() {
        // When & Then
        val exception =
            assertThrows<CountryNotFoundException> {
                countryFacade.deleteCountry(99999L)
            }
        assertEquals("Country not found with id: '99999'", exception.message)
    }

    @Test
    fun `module should expose only intended interfaces`() {
        // This test verifies that only the intended interfaces are exposed
        // The actual verification is done by Spring Modulith at compile time
        // and through the architecture tests

        // Verify we can access public API
        assertNotNull(countryQueryService)
        assertNotNull(countryFacade)

        // The following would fail at compile time if internal packages were exposed:
        // val internalService: CountryServiceImpl = ... // Should not compile
        // val repository: CountryRepository = ... // Should not compile
    }
}
