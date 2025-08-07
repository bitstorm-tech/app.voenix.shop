package com.jotoai.voenix.shop.country.internal

import com.jotoai.voenix.shop.country.internal.entity.Country
import com.jotoai.voenix.shop.country.internal.repository.CountryRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
class CountryRepositoryTest {
    @Autowired
    private lateinit var testEntityManager: TestEntityManager

    @Autowired
    private lateinit var countryRepository: CountryRepository

    @BeforeEach
    fun setUp() {
        // Clean up any existing data
        countryRepository.deleteAll()
        testEntityManager.flush()
    }

    @Test
    fun `should save and retrieve country`() {
        // Given
        val country =
            Country(
                name = "Germany",
            )

        // When
        val savedCountry = countryRepository.save(country)
        testEntityManager.flush()
        testEntityManager.clear()

        val retrievedCountry = countryRepository.findById(savedCountry.id!!).orElse(null)

        // Then
        assertNotNull(retrievedCountry)
        assertEquals("Germany", retrievedCountry.name)
        assertNotNull(retrievedCountry.createdAt)
        assertNotNull(retrievedCountry.updatedAt)
    }

    @Test
    fun `should find all countries with pagination`() {
        // Given
        val country1 =
            Country(
                name = "Germany",
            )
        val country2 =
            Country(
                name = "France",
            )
        val country3 =
            Country(
                name = "Italy",
            )

        countryRepository.save(country1)
        countryRepository.save(country2)
        countryRepository.save(country3)
        testEntityManager.flush()

        // When
        val pageable = PageRequest.of(0, 2, Sort.by("name"))
        val page = countryRepository.findAll(pageable)

        // Then
        assertEquals(2, page.content.size)
        assertEquals(3, page.totalElements)
        assertEquals(2, page.totalPages)
        assertEquals("France", page.content[0].name)
        assertEquals("Germany", page.content[1].name)
    }

    @Test
    fun `should update country`() {
        // Given
        val country =
            Country(
                name = "Germany",
            )
        val savedCountry = countryRepository.save(country)
        testEntityManager.flush()
        testEntityManager.clear()

        // When
        val countryToUpdate = countryRepository.findById(savedCountry.id!!).orElse(null)
        countryToUpdate.name = "Deutschland"

        val updatedCountry = countryRepository.save(countryToUpdate)
        testEntityManager.flush()
        testEntityManager.clear()

        val retrievedCountry = countryRepository.findById(updatedCountry.id!!).orElse(null)

        // Then
        assertNotNull(retrievedCountry)
        assertEquals("Deutschland", retrievedCountry.name)
    }

    @Test
    fun `should delete country`() {
        // Given
        val country =
            Country(
                name = "Germany",
            )
        val savedCountry = countryRepository.save(country)
        testEntityManager.flush()

        // When
        countryRepository.deleteById(savedCountry.id!!)
        testEntityManager.flush()

        val retrievedCountry = countryRepository.findById(savedCountry.id!!).orElse(null)

        // Then
        assertTrue(retrievedCountry == null)
    }
}
