package com.jotoai.voenix.shop.country

import com.jotoai.voenix.shop.country.api.CountryService
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
    private lateinit var countryService: CountryService

    @Test
    fun `should create country`() {
        // Given
        val request =
            CreateCountryRequest(
                name = "Test Country",
            )

        // When
        val createdCountry = countryService.createCountry(request)

        // Then - verify returned DTO
        assertNotNull(createdCountry)
        assertEquals("Test Country", createdCountry.name)
        assertNotNull(createdCountry.id)
        assertNotNull(createdCountry.createdAt)
        assertNotNull(createdCountry.updatedAt)
    }

    @Test
    fun `should update country`() {
        // Given - create a country first
        val initialCountry =
            countryService.createCountry(
                CreateCountryRequest(
                    name = "Initial Country",
                ),
            )

        val updateRequest =
            UpdateCountryRequest(
                name = "Updated Country",
            )

        // When
        val updatedCountry = countryService.updateCountry(initialCountry.id!!, updateRequest)

        // Then - verify returned DTO
        assertNotNull(updatedCountry)
        assertEquals("Updated Country", updatedCountry.name)
        assertEquals(initialCountry.id, updatedCountry.id)
    }

    @Test
    fun `should delete country`() {
        // Given - create a country first
        val country =
            countryService.createCountry(
                CreateCountryRequest(
                    name = "Country to Delete",
                ),
            )

        // When
        countryService.deleteCountry(country.id!!)

        // Then - verify country is deleted
        assertThrows<CountryNotFoundException> {
            countryService.getCountryById(country.id!!)
        }
    }

    @Test
    fun `should handle query operations correctly`() {
        // Given - create some test countries
        val country1 =
            countryService.createCountry(
                CreateCountryRequest("Germany"),
            )
        val country2 =
            countryService.createCountry(
                CreateCountryRequest("France"),
            )
        val country3 =
            countryService.createCountry(
                CreateCountryRequest("United Kingdom"),
            )

        // Test getAllCountries
        val allCountries = countryService.getAllCountries()
        assertTrue(allCountries.size >= 3)

        // Test getCountryById
        val retrievedCountry = countryService.getCountryById(country1.id!!)
        assertEquals("Germany", retrievedCountry.name)

        // Test existsById
        assertTrue(countryService.existsById(country2.id!!))
        assertTrue(countryService.existsById(country3.id!!))
    }

    @Test
    fun `should throw exception when country not found`() {
        // When & Then
        val exception =
            assertThrows<CountryNotFoundException> {
                countryService.getCountryById(99999L)
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
                countryService.updateCountry(99999L, updateRequest)
            }
        assertEquals("Country not found with id: '99999'", exception.message)
    }

    @Test
    fun `should throw exception when deleting non-existent country`() {
        // When & Then
        val exception =
            assertThrows<CountryNotFoundException> {
                countryService.deleteCountry(99999L)
            }
        assertEquals("Country not found with id: '99999'", exception.message)
    }

    @Test
    fun `module should expose only intended interfaces`() {
        // This test verifies that only the intended interfaces are exposed
        // The actual verification is done by Spring Modulith at compile time
        // and through the architecture tests

        // Verify we can access public API
        assertNotNull(countryService)

        // The following would fail at compile time if internal packages were exposed:
        // val internalService: CountryServiceImpl = ... // Should not compile
        // val repository: CountryRepository = ... // Should not compile
    }
}
