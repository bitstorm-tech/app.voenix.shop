package com.jotoai.voenix.shop.supplier.internal.service

import com.jotoai.voenix.shop.country.api.CountryService
import com.jotoai.voenix.shop.country.api.exceptions.CountryNotFoundException
import com.jotoai.voenix.shop.supplier.api.SupplierQueryService
import com.jotoai.voenix.shop.supplier.api.dto.SupplierDto
import com.jotoai.voenix.shop.supplier.api.exceptions.SupplierNotFoundException
import com.jotoai.voenix.shop.supplier.internal.repository.SupplierRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of SupplierQueryService for read operations.
 *
 * This service handles all read-only operations for suppliers, including
 * querying suppliers with their associated country information using
 * direct service dependencies.
 */
@Service
@Transactional(readOnly = true)
class SupplierQueryServiceImpl(
    private val supplierRepository: SupplierRepository,
    private val countryService: CountryService,
) : SupplierQueryService {
    override fun getAllSuppliers(): List<SupplierDto> =
        supplierRepository.findAll().map { supplier ->
            val countryDto =
                supplier.countryId?.let { countryId ->
                    try {
                        countryService.getCountryById(countryId)
                    } catch (e: CountryNotFoundException) {
                        null
                    }
                }
            supplier.toDtoWithCountry(countryDto)
        }

    override fun getSupplierById(id: Long): SupplierDto {
        val supplier =
            supplierRepository
                .findById(id)
                .orElseThrow { SupplierNotFoundException("Supplier", "id", id) }

        val countryDto =
            supplier.countryId?.let { countryId ->
                try {
                    countryService.getCountryById(countryId)
                } catch (e: CountryNotFoundException) {
                    null
                }
            }

        return supplier.toDtoWithCountry(countryDto)
    }

    override fun existsById(id: Long): Boolean = supplierRepository.existsById(id)
}
