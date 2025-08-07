package com.jotoai.voenix.shop.supplier.internal.service

import com.jotoai.voenix.shop.supplier.api.SupplierQueryService
import com.jotoai.voenix.shop.supplier.api.dto.SupplierDto
import com.jotoai.voenix.shop.supplier.api.exceptions.SupplierNotFoundException
import com.jotoai.voenix.shop.supplier.internal.country.CountryEventListener
import com.jotoai.voenix.shop.supplier.internal.repository.SupplierRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of SupplierQueryService for read operations.
 *
 * This service handles all read-only operations for suppliers, including
 * querying suppliers with their associated country information using
 * the event-driven approach to avoid direct dependencies on the country module.
 */
@Service
@Transactional(readOnly = true)
class SupplierQueryServiceImpl(
    private val supplierRepository: SupplierRepository,
    private val countryEventListener: CountryEventListener,
) : SupplierQueryService {
    override fun getAllSuppliers(): List<SupplierDto> =
        supplierRepository.findAll().map { supplier ->
            val countryData =
                supplier.countryId?.let { countryId ->
                    countryEventListener.getCountryById(countryId)
                }
            supplier.toDtoWithCountry(countryData)
        }

    override fun getSupplierById(id: Long): SupplierDto {
        val supplier =
            supplierRepository
                .findById(id)
                .orElseThrow { SupplierNotFoundException("Supplier", "id", id) }

        val countryData =
            supplier.countryId?.let { countryId ->
                countryEventListener.getCountryById(countryId)
            }

        return supplier.toDtoWithCountry(countryData)
    }

    override fun existsById(id: Long): Boolean = supplierRepository.existsById(id)
}
