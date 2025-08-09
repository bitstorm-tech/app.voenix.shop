package com.jotoai.voenix.shop.supplier.internal.service

import com.jotoai.voenix.shop.country.api.dto.CountryDto
import com.jotoai.voenix.shop.supplier.api.dto.CreateSupplierRequest
import com.jotoai.voenix.shop.supplier.api.dto.SupplierDto
import com.jotoai.voenix.shop.supplier.api.dto.UpdateSupplierRequest
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
    request.name?.let { this.name = it.trim() }
    request.title?.let { this.title = it.trim() }
    request.firstName?.let { this.firstName = it.trim() }
    request.lastName?.let { this.lastName = it.trim() }
    request.street?.let { this.street = it.trim() }
    request.houseNumber?.let { this.houseNumber = it.trim() }
    request.city?.let { this.city = it.trim() }
    request.postalCode?.let { this.postalCode = it }
    request.phoneNumber1?.let { this.phoneNumber1 = it.trim() }
    request.phoneNumber2?.let { this.phoneNumber2 = it.trim() }
    request.phoneNumber3?.let { this.phoneNumber3 = it.trim() }
    request.email?.let { this.email = it.trim() }
    request.website?.let { this.website = it.trim() }
    request.countryId?.let { this.countryId = it }
}

fun Supplier.toDtoWithCountry(countryDto: CountryDto?): SupplierDto {
    val baseDto = this.toDto()
    return baseDto.copy(
        countryName = countryDto?.name,
    )
}
