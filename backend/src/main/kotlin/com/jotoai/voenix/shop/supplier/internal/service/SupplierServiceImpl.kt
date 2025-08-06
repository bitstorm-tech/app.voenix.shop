package com.jotoai.voenix.shop.supplier.internal.service

import com.jotoai.voenix.shop.country.api.CountryQueryService
import com.jotoai.voenix.shop.supplier.api.SupplierFacade
import com.jotoai.voenix.shop.supplier.api.SupplierQueryService
import com.jotoai.voenix.shop.supplier.api.dto.CreateSupplierRequest
import com.jotoai.voenix.shop.supplier.api.dto.SupplierDto
import com.jotoai.voenix.shop.supplier.api.dto.UpdateSupplierRequest
import com.jotoai.voenix.shop.supplier.events.SupplierCreatedEvent
import com.jotoai.voenix.shop.supplier.events.SupplierDeletedEvent
import com.jotoai.voenix.shop.supplier.events.SupplierUpdatedEvent
import com.jotoai.voenix.shop.supplier.internal.entity.Supplier
import com.jotoai.voenix.shop.supplier.internal.exception.SupplierNotFoundException
import com.jotoai.voenix.shop.supplier.internal.repository.SupplierRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SupplierServiceImpl(
    private val supplierRepository: SupplierRepository,
    private val countryQueryService: CountryQueryService,
    private val eventPublisher: ApplicationEventPublisher,
) : SupplierFacade,
    SupplierQueryService {
    override fun getAllSuppliers(): List<SupplierDto> = supplierRepository.findAll().map { it.toDto() }

    override fun getSupplierById(id: Long): SupplierDto =
        supplierRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { SupplierNotFoundException("Supplier", "id", id) }

    private fun validateUniqueConstraints(
        name: String?,
        email: String?,
        excludeId: Long? = null,
    ) {
        if (!name.isNullOrBlank()) {
            if (excludeId != null) {
                if (supplierRepository.existsByNameAndIdNot(name, excludeId)) {
                    throw IllegalArgumentException("Supplier with name '$name' already exists")
                }
            } else {
                if (supplierRepository.existsByName(name)) {
                    throw IllegalArgumentException("Supplier with name '$name' already exists")
                }
            }
        }

        if (!email.isNullOrBlank()) {
            if (excludeId != null) {
                if (supplierRepository.existsByEmailAndIdNot(email, excludeId)) {
                    throw IllegalArgumentException("Supplier with email '$email' already exists")
                }
            } else {
                if (supplierRepository.existsByEmail(email)) {
                    throw IllegalArgumentException("Supplier with email '$email' already exists")
                }
            }
        }
    }

    override fun existsById(id: Long): Boolean = supplierRepository.existsById(id)

    @Deprecated("Use getSupplierById instead and refactor entity relationships")
    override fun getSupplierEntityReference(id: Long): Any =
        supplierRepository
            .findById(id)
            .orElseThrow { SupplierNotFoundException("Supplier", "id", id) }

    @Transactional
    override fun createSupplier(request: CreateSupplierRequest): SupplierDto {
        // Validate unique constraints
        validateUniqueConstraints(request.name, request.email)

        // Fetch country if provided
        val country =
            request.countryId?.let { countryId ->
                @Suppress("DEPRECATION")
                countryQueryService.getCountryEntityReference(countryId) as com.jotoai.voenix.shop.country.internal.entity.Country
            }

        val supplier =
            Supplier(
                name = request.name?.trim(),
                title = request.title?.trim(),
                firstName = request.firstName?.trim(),
                lastName = request.lastName?.trim(),
                street = request.street?.trim(),
                houseNumber = request.houseNumber?.trim(),
                city = request.city?.trim(),
                postalCode = request.postalCode,
                country = country,
                phoneNumber1 = request.phoneNumber1?.trim(),
                phoneNumber2 = request.phoneNumber2?.trim(),
                phoneNumber3 = request.phoneNumber3?.trim(),
                email = request.email?.trim(),
                website = request.website?.trim(),
            )

        val savedSupplier = supplierRepository.save(supplier)
        val result = savedSupplier.toDto()

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

        val oldDto = supplier.toDto()

        // Validate unique constraints
        validateUniqueConstraints(request.name, request.email, id)

        // Update fields directly with mutable properties
        request.name?.let { supplier.name = it.trim() }
        request.title?.let { supplier.title = it.trim() }
        request.firstName?.let { supplier.firstName = it.trim() }
        request.lastName?.let { supplier.lastName = it.trim() }
        request.street?.let { supplier.street = it.trim() }
        request.houseNumber?.let { supplier.houseNumber = it.trim() }
        request.city?.let { supplier.city = it.trim() }
        request.postalCode?.let { supplier.postalCode = it }
        request.phoneNumber1?.let { supplier.phoneNumber1 = it.trim() }
        request.phoneNumber2?.let { supplier.phoneNumber2 = it.trim() }
        request.phoneNumber3?.let { supplier.phoneNumber3 = it.trim() }
        request.email?.let { supplier.email = it.trim() }
        request.website?.let { supplier.website = it.trim() }

        // Update country if provided
        request.countryId?.let { countryId ->
            @Suppress("DEPRECATION")
            supplier.country =
                countryQueryService.getCountryEntityReference(countryId) as com.jotoai.voenix.shop.country.internal.entity.Country
        }

        val savedSupplier = supplierRepository.save(supplier)
        val result = savedSupplier.toDto()

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

        val supplierDto = supplier.toDto()

        supplierRepository.deleteById(id)

        // Publish event
        eventPublisher.publishEvent(SupplierDeletedEvent(supplierDto))
    }
}
