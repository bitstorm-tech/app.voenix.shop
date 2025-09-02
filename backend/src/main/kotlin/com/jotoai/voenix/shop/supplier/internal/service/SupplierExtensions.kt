package com.jotoai.voenix.shop.supplier.internal.service

import com.jotoai.voenix.shop.country.CountryDto
import com.jotoai.voenix.shop.supplier.CreateSupplierRequest
import com.jotoai.voenix.shop.supplier.SupplierDto
import com.jotoai.voenix.shop.supplier.UpdateSupplierRequest
import com.jotoai.voenix.shop.supplier.internal.entity.Supplier

/**
 * Extension functions for entity mapping and validation helpers.
 * These functions provide reusable logic for supplier operations.
 */

fun CreateSupplierRequest.toEntity(): Supplier =
    Supplier(
        name = this.name?.trim(),
        title = this.title?.trim(),
        firstName = this.firstName?.trim(),
        lastName = this.lastName?.trim(),
        street = this.street?.trim(),
        houseNumber = this.houseNumber?.trim(),
        city = this.city?.trim(),
        postalCode = this.postalCode,
        countryId = this.countryId,
        phoneNumber1 = this.phoneNumber1?.trim(),
        phoneNumber2 = this.phoneNumber2?.trim(),
        phoneNumber3 = this.phoneNumber3?.trim(),
        email = this.email?.trim(),
        website = this.website?.trim(),
    )

fun Supplier.updateFrom(request: UpdateSupplierRequest) {
    request.name?.let { name = it.trim() }
    request.title?.let { title = it.trim() }
    request.firstName?.let { firstName = it.trim() }
    request.lastName?.let { lastName = it.trim() }
    request.street?.let { street = it.trim() }
    request.houseNumber?.let { houseNumber = it.trim() }
    request.city?.let { city = it.trim() }
    request.postalCode?.let { postalCode = it }
    request.countryId?.let { countryId = it }
    request.phoneNumber1?.let { phoneNumber1 = it.trim() }
    request.phoneNumber2?.let { phoneNumber2 = it.trim() }
    request.phoneNumber3?.let { phoneNumber3 = it.trim() }
    request.email?.let { email = it.trim() }
    request.website?.let { website = it.trim() }
}

fun Supplier.toDtoWithCountry(countryDto: CountryDto?): SupplierDto = toDto().copy(countryName = countryDto?.name)
