package com.jotoai.voenix.shop.supplier.internal.service

import com.jotoai.voenix.shop.supplier.api.SupplierFacade
import com.jotoai.voenix.shop.supplier.api.dto.CreateSupplierRequest
import com.jotoai.voenix.shop.supplier.api.dto.SupplierDto
import com.jotoai.voenix.shop.supplier.api.dto.UpdateSupplierRequest
import com.jotoai.voenix.shop.supplier.api.exceptions.SupplierNotFoundException
import com.jotoai.voenix.shop.supplier.events.SupplierCreatedEvent
import com.jotoai.voenix.shop.supplier.events.SupplierDeletedEvent
import com.jotoai.voenix.shop.supplier.events.SupplierUpdatedEvent
import com.jotoai.voenix.shop.supplier.internal.country.CountryEventListener
import com.jotoai.voenix.shop.supplier.internal.repository.SupplierRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of SupplierFacade for write operations.
 *
 * This service handles all write operations for suppliers, including
 * creation, updates, and deletion. It publishes events for these operations
 * and uses the event-driven approach for country data to maintain
 * Spring Modulith compliance.
 */
@Service
@Transactional(readOnly = true)
class SupplierFacadeImpl(
    private val supplierRepository: SupplierRepository,
    private val countryEventListener: CountryEventListener,
    private val eventPublisher: ApplicationEventPublisher,
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
        val countryData =
            savedSupplier.countryId?.let { countryId ->
                countryEventListener.getCountryById(countryId)
            }
        val result = savedSupplier.toDtoWithCountry(countryData)

        // Publish event
        eventPublisher.publishEvent(SupplierCreatedEvent(result))

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
                    countryEventListener.getCountryById(countryId)
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
        val countryData =
            savedSupplier.countryId?.let { countryId ->
                countryEventListener.getCountryById(countryId)
            }
        val result = savedSupplier.toDtoWithCountry(countryData)

        // Publish event
        eventPublisher.publishEvent(SupplierUpdatedEvent(oldDto, result))

        return result
    }

    @Transactional
    override fun deleteSupplier(id: Long) {
        val supplier =
            supplierRepository
                .findById(id)
                .orElseThrow { SupplierNotFoundException("Supplier", "id", id) }

        val countryData =
            supplier.countryId?.let { countryId ->
                countryEventListener.getCountryById(countryId)
            }
        val supplierDto = supplier.toDtoWithCountry(countryData)

        supplierRepository.deleteById(id)

        // Publish event
        eventPublisher.publishEvent(SupplierDeletedEvent(supplierDto))
    }
}
