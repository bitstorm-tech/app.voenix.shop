package com.jotoai.voenix.shop.supplier.internal.service

import com.jotoai.voenix.shop.country.api.CountryQueryService
import com.jotoai.voenix.shop.country.api.exceptions.CountryNotFoundException
import com.jotoai.voenix.shop.supplier.api.SupplierFacade
import com.jotoai.voenix.shop.supplier.api.dto.CreateSupplierRequest
import com.jotoai.voenix.shop.supplier.api.dto.SupplierDto
import com.jotoai.voenix.shop.supplier.api.dto.UpdateSupplierRequest
import com.jotoai.voenix.shop.supplier.api.exceptions.SupplierNotFoundException
import com.jotoai.voenix.shop.supplier.internal.repository.SupplierRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of SupplierFacade for write operations.
 *
 * This service handles all write operations for suppliers, including
 * creation, updates, and deletion. It publishes events for these operations
 * and uses direct service calls for country data.
 */
@Service
@Transactional(readOnly = true)
class SupplierFacadeImpl(
    private val supplierRepository: SupplierRepository,
    private val countryQueryService: CountryQueryService,
    private val validation: SupplierValidation,
) : SupplierFacade {
    @Transactional
    override fun createSupplier(request: CreateSupplierRequest): SupplierDto {
        // Validate unique constraints
        validation.validateUniqueField(request.name, request.email)

        // Validate country exists if provided
        validation.validateCountryExists(request.countryId)

        // Create and save the supplier
        val supplier = request.toEntity()
        val savedSupplier = supplierRepository.save(supplier)

        // Get country data for DTO
        val countryDto =
            savedSupplier.countryId?.let { countryId ->
                try {
                    countryQueryService.getCountryById(countryId)
                } catch (e: CountryNotFoundException) {
                    null
                }
            }
        val result = savedSupplier.toDtoWithCountry(countryDto)

        return result
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

        val oldDto =
            supplier.toDtoWithCountry(
                supplier.countryId?.let { countryId ->
                    try {
                        countryQueryService.getCountryById(countryId)
                    } catch (e: CountryNotFoundException) {
                        null
                    }
                },
            )

        // Validate unique constraints
        validation.validateUniqueField(request.name, request.email, id)

        // Validate country exists if provided
        validation.validateCountryExists(request.countryId)

        // Update supplier fields
        supplier.updateFrom(request)

        val savedSupplier = supplierRepository.save(supplier)

        // Get country data for DTO
        val countryDto =
            savedSupplier.countryId?.let { countryId ->
                try {
                    countryQueryService.getCountryById(countryId)
                } catch (e: CountryNotFoundException) {
                    null
                }
            }
        val result = savedSupplier.toDtoWithCountry(countryDto)

        return result
    }

    @Transactional
    override fun deleteSupplier(id: Long) {
        val supplier =
            supplierRepository
                .findById(id)
                .orElseThrow { SupplierNotFoundException("Supplier", "id", id) }

        val countryDto =
            supplier.countryId?.let { countryId ->
                try {
                    countryQueryService.getCountryById(countryId)
                } catch (e: CountryNotFoundException) {
                    null
                }
            }
        val supplierDto = supplier.toDtoWithCountry(countryDto)

        supplierRepository.deleteById(id)
    }
}
