package com.jotoai.voenix.shop.supplier.internal.service

import com.jotoai.voenix.shop.country.api.CountryService
import com.jotoai.voenix.shop.country.api.dto.CountryDto
import com.jotoai.voenix.shop.country.api.exceptions.CountryNotFoundException
import com.jotoai.voenix.shop.supplier.api.SupplierService
import com.jotoai.voenix.shop.supplier.api.dto.CreateSupplierRequest
import com.jotoai.voenix.shop.supplier.api.dto.SupplierDto
import com.jotoai.voenix.shop.supplier.api.dto.UpdateSupplierRequest
import com.jotoai.voenix.shop.supplier.api.exceptions.DuplicateSupplierException
import com.jotoai.voenix.shop.supplier.api.exceptions.InvalidSupplierDataException
import com.jotoai.voenix.shop.supplier.api.exceptions.SupplierNotFoundException
import com.jotoai.voenix.shop.supplier.internal.repository.SupplierRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Unified implementation of SupplierService that combines read and write operations.
 *
 * This service handles all supplier operations, including creation, updates, deletion,
 * and queries. It merges the functionality previously split between SupplierFacadeImpl
 * and SupplierQueryServiceImpl, while integrating validation logic directly.
 */
@Service
@Transactional(readOnly = true)
class SupplierServiceImpl(
    private val supplierRepository: SupplierRepository,
    private val countryService: CountryService,
) : SupplierService {
    private val logger = KotlinLogging.logger {}

    // Query operations

    override fun getAllSuppliers(): List<SupplierDto> {
        val suppliers = supplierRepository.findAll()

        // Collect all unique country IDs
        val countryIds = suppliers.mapNotNull { it.countryId }.distinct()

        // Fetch all countries at once and create a lookup map
        val countryMap =
            if (countryIds.isNotEmpty()) {
                countryService
                    .getAllCountries()
                    .filter { it.id in countryIds }
                    .associateBy { it.id }
            } else {
                emptyMap()
            }

        // Map suppliers with their country data from the lookup map
        return suppliers.map { supplier ->
            val countryDto = supplier.countryId?.let { countryMap[it] }
            supplier.toDtoWithCountry(countryDto)
        }
    }

    override fun getSupplierById(id: Long): SupplierDto {
        val supplier =
            supplierRepository
                .findById(id)
                .orElseThrow { SupplierNotFoundException("Supplier", "id", id) }

        val countryDto = getCountryDto(supplier.countryId)
        return supplier.toDtoWithCountry(countryDto)
    }

    override fun existsById(id: Long): Boolean = supplierRepository.existsById(id)

    // Command operations

    @Transactional
    override fun createSupplier(request: CreateSupplierRequest): SupplierDto {
        // Validate unique constraints
        validateUniqueField(request.name, request.email)

        // Validate country exists if provided
        validateCountryExists(request.countryId)

        // Create and save the supplier
        val supplier = request.toEntity()
        val savedSupplier = supplierRepository.save(supplier)

        // Get country data for DTO
        val countryDto = getCountryDto(savedSupplier.countryId)
        return savedSupplier.toDtoWithCountry(countryDto)
    }

    @Transactional
    override fun updateSupplier(
        id: Long,
        request: UpdateSupplierRequest,
    ): SupplierDto {
        val supplier =
            supplierRepository
                .findById(id)
                .orElseThrow { SupplierNotFoundException("Supplier", "id", id) }

        // Validate unique constraints
        validateUniqueField(request.name, request.email, id)

        // Validate country exists if provided
        validateCountryExists(request.countryId)

        // Update supplier fields
        supplier.updateFrom(request)

        val savedSupplier = supplierRepository.save(supplier)

        // Get country data for DTO
        val countryDto = getCountryDto(savedSupplier.countryId)
        return savedSupplier.toDtoWithCountry(countryDto)
    }

    @Transactional
    override fun deleteSupplier(id: Long) {
        supplierRepository.deleteById(id)
    }

    // Private helper methods (integrated validation logic)

    /**
     * Helper method to fetch country DTO, avoiding duplicate logic.
     * Safely handles CountryNotFoundException by returning null.
     */
    private fun getCountryDto(countryId: Long?): CountryDto? =
        countryId?.let { id ->
            try {
                countryService.getCountryById(id)
            } catch (e: CountryNotFoundException) {
                logger.debug(e) { "Country with ID $id not found when fetching supplier country details" }
                null
            }
        }

    /**
     * Validates unique field constraints for suppliers.
     *
     * @param name the supplier name to validate (can be null)
     * @param email the supplier email to validate (can be null)
     * @param excludeId optional ID to exclude from uniqueness check (for updates)
     * @throws DuplicateSupplierException if validation fails
     */
    private fun validateUniqueField(
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
    private fun validateCountryExists(countryId: Long?) {
        if (countryId != null && !countryService.existsById(countryId)) {
            throw InvalidSupplierDataException("countryId", countryId, "does not exist")
        }
    }
}
