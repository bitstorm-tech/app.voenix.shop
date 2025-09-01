package com.jotoai.voenix.shop.order.internal.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class Address(
    @Column(name = "street_address_1", nullable = false, length = 255)
    val streetAddress1: String,
    @Column(name = "street_address_2", nullable = true, length = 255)
    val streetAddress2: String? = null,
    @Column(name = "city", nullable = false, length = 100)
    val city: String,
    @Column(name = "state", nullable = false, length = 100)
    val state: String,
    @Column(name = "postal_code", nullable = false, length = 20)
    val postalCode: String,
    @Column(name = "country", nullable = false, length = 100)
    val country: String = "USA",
)
