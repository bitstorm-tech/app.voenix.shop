package com.jotoai.voenix.shop.domain.orders.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class Address(
    @Column(name = "street_address_1", nullable = false, length = 255)
    var streetAddress1: String,
    @Column(name = "street_address_2", nullable = true, length = 255)
    var streetAddress2: String? = null,
    @Column(name = "city", nullable = false, length = 100)
    var city: String,
    @Column(name = "state", nullable = false, length = 100)
    var state: String,
    @Column(name = "postal_code", nullable = false, length = 20)
    var postalCode: String,
    @Column(name = "country", nullable = false, length = 100)
    var country: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Address) return false
        return streetAddress1 == other.streetAddress1 &&
            streetAddress2 == other.streetAddress2 &&
            city == other.city &&
            state == other.state &&
            postalCode == other.postalCode &&
            country == other.country
    }

    override fun hashCode(): Int {
        var result = streetAddress1.hashCode()
        result = 31 * result + (streetAddress2?.hashCode() ?: 0)
        result = 31 * result + city.hashCode()
        result = 31 * result + state.hashCode()
        result = 31 * result + postalCode.hashCode()
        result = 31 * result + country.hashCode()
        return result
    }

    override fun toString(): String =
        "Address(streetAddress1='$streetAddress1', streetAddress2=$streetAddress2, city='$city', state='$state', postalCode='$postalCode', country='$country')"
}
