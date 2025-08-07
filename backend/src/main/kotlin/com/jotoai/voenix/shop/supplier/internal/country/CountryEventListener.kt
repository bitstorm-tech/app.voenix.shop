package com.jotoai.voenix.shop.supplier.internal.country

import com.jotoai.voenix.shop.country.events.CountryCreatedEvent
import com.jotoai.voenix.shop.country.events.CountryDeletedEvent
import com.jotoai.voenix.shop.country.events.CountryUpdatedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

/**
 * Event listener that maintains a local cache of country data within the supplier module.
 *
 * This component listens to country events from the country module and maintains
 * a local cache to avoid direct dependencies. This follows Spring Modulith principles
 * by using event-driven architecture for cross-module communication.
 */
@Component
class CountryEventListener {
    private val logger = LoggerFactory.getLogger(CountryEventListener::class.java)
    private val countryCache = ConcurrentHashMap<Long, CountryData>()

    /**
     * Handles country creation events by adding the country to the local cache.
     */
    @EventListener
    fun handleCountryCreated(event: CountryCreatedEvent) {
        logger.debug("Handling country created event for country: {}", event.country.name)
        val countryData =
            CountryData(
                id = event.country.id,
                name = event.country.name,
            )
        countryCache[event.country.id] = countryData
        logger.info("Country cached: {} (ID: {})", event.country.name, event.country.id)
    }

    /**
     * Handles country update events by updating the country in the local cache.
     */
    @EventListener
    fun handleCountryUpdated(event: CountryUpdatedEvent) {
        logger.debug("Handling country updated event for country: {}", event.newCountry.name)
        val countryData =
            CountryData(
                id = event.newCountry.id,
                name = event.newCountry.name,
            )
        countryCache[event.newCountry.id] = countryData
        logger.info("Country cache updated: {} (ID: {})", event.newCountry.name, event.newCountry.id)
    }

    /**
     * Handles country deletion events by removing the country from the local cache.
     */
    @EventListener
    fun handleCountryDeleted(event: CountryDeletedEvent) {
        logger.debug("Handling country deleted event for country: {}", event.country.name)
        countryCache.remove(event.country.id)
        logger.info("Country removed from cache: {} (ID: {})", event.country.name, event.country.id)
    }

    /**
     * Retrieves country data from the local cache.
     *
     * @param countryId the ID of the country to retrieve
     * @return the country data if found, null otherwise
     */
    fun getCountryById(countryId: Long): CountryData? = countryCache[countryId]

    /**
     * Checks if a country exists in the local cache.
     *
     * @param countryId the ID of the country to check
     * @return true if the country exists, false otherwise
     */
    fun existsById(countryId: Long): Boolean = countryCache.containsKey(countryId)

    /**
     * Gets all cached countries.
     *
     * @return a map of all cached countries
     */
    fun getAllCountries(): Map<Long, CountryData> = countryCache.toMap()
}
