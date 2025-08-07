package com.jotoai.voenix.shop.supplier.internal.country

/**
 * Internal representation of country data within the supplier module.
 *
 * This data class is used to maintain a local cache of country information
 * that the supplier module needs without creating a direct dependency on
 * the country module. Data is populated through event listeners.
 *
 * @param id unique identifier of the country
 * @param name human-readable name of the country
 */
data class CountryData(
    val id: Long,
    val name: String,
)
