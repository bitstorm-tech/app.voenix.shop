package com.jotoai.voenix.shop.supplier.internal.service

import com.jotoai.voenix.shop.application.BadRequestException
import com.jotoai.voenix.shop.application.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.application.ResourceNotFoundException
import com.jotoai.voenix.shop.country.CountryDto
import com.jotoai.voenix.shop.country.CountryNotFoundException
import com.jotoai.voenix.shop.country.CountryService
import com.jotoai.voenix.shop.supplier.CreateSupplierRequest
import com.jotoai.voenix.shop.supplier.SupplierDto
import com.jotoai.voenix.shop.supplier.SupplierService
import com.jotoai.voenix.shop.supplier.UpdateSupplierRequest
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
                .orElseThrow { ResourceNotFoundException("Supplier", "id", id) }

        val countryDto = getCountryDto(supplier.countryId)
        return supplier.toDtoWithCountry(countryDto)
    }

    override fun existsById(id: Long): Boolean = supplierRepository.existsById(id)

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
                .orElseThrow { ResourceNotFoundException("Supplier", "id", id) }

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
     * @throws ResourceAlreadyExistsException if validation fails
     */
    private fun validateUniqueField(
        name: String?,
        email: String?,
        excludeId: Long? = null,
    ) {
        name?.takeIf { it.isNotBlank() }?.let {
            val exists =
                excludeId?.let { id -> supplierRepository.existsByNameAndIdNot(it, id) }
                    ?: supplierRepository.existsByName(it)
            if (exists) throw ResourceAlreadyExistsException("Supplier", "name", it)
        }

        email?.takeIf { it.isNotBlank() }?.let {
            val exists =
                excludeId?.let { id -> supplierRepository.existsByEmailAndIdNot(it, id) }
                    ?: supplierRepository.existsByEmail(it)
            if (exists) throw ResourceAlreadyExistsException("Supplier", "email", it)
        }
    }

    /**
     * Validates that a country exists.
     *
     * @param countryId the country ID to validate (can be null)
     * @throws BadRequestException if country doesn't exist
     */
    private fun validateCountryExists(countryId: Long?) {
        if (countryId != null && !countryService.existsById(countryId)) {
            throw BadRequestException("Country with ID $countryId does not exist")
        }
    }
}
