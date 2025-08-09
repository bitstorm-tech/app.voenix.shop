package com.jotoai.voenix.shop.supplier.internal.service

import com.jotoai.voenix.shop.country.api.CountryQueryService
import com.jotoai.voenix.shop.supplier.api.exceptions.DuplicateSupplierException
import com.jotoai.voenix.shop.supplier.api.exceptions.InvalidSupplierDataException
import com.jotoai.voenix.shop.supplier.internal.repository.SupplierRepository
import org.springframework.stereotype.Component

/**
 * Validation helper methods for supplier operations.
 */
@Component
class SupplierValidation(
    private val supplierRepository: SupplierRepository,
    private val countryQueryService: CountryQueryService,
) {
    /**
     * Validates unique field constraints for suppliers.
     *
     * @param name the supplier name to validate (can be null)
     * @param email the supplier email to validate (can be null)
     * @param excludeId optional ID to exclude from uniqueness check (for updates)
     * @throws DuplicateSupplierException if validation fails
     */
    fun validateUniqueField(
        name: String?,
        email: String?,
        excludeId: Long? = null,
    ) {
        if (!name.isNullOrBlank()) {
            val nameExists =
                if (excludeId != null) {
                    supplierRepository.existsByNameAndIdNot(name, excludeId)
                } else {
                    supplierRepository.existsByName(name)
                }

            if (nameExists) {
                throw DuplicateSupplierException("name", name)
            }
        }

        if (!email.isNullOrBlank()) {
            val emailExists =
                if (excludeId != null) {
                    supplierRepository.existsByEmailAndIdNot(email, excludeId)
                } else {
                    supplierRepository.existsByEmail(email)
                }

            if (emailExists) {
                throw DuplicateSupplierException("email", email)
            }
        }
    }

    /**
     * Validates that a country exists.
     *
     * @param countryId the country ID to validate (can be null)
     * @throws InvalidSupplierDataException if country doesn't exist
     */
    fun validateCountryExists(countryId: Long?) {
        if (countryId != null && !countryQueryService.existsById(countryId)) {
            throw InvalidSupplierDataException("countryId", countryId, "does not exist")
        }
    }
}
