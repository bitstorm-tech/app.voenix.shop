package com.jotoai.voenix.shop.supplier.internal.service

import com.jotoai.voenix.shop.domain.countries.service.CountryService
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
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class SupplierServiceImpl(
    private val supplierRepository: SupplierRepository,
    private val countryService: CountryService,
    private val eventPublisher: ApplicationEventPublisher,
) : SupplierFacade,
    SupplierQueryService {
    override fun getAllSuppliers(): List<SupplierDto> = supplierRepository.findAll().map { it.toDto() }

    override fun getSupplierById(id: Long): SupplierDto =
        supplierRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { SupplierNotFoundException("Supplier", "id", id) }

    override fun existsById(id: Long): Boolean = supplierRepository.existsById(id)

    @Deprecated("Use getSupplierById instead and refactor entity relationships")
    override fun getSupplierEntityReference(id: Long): Any =
        supplierRepository
            .findById(id)
            .orElseThrow { SupplierNotFoundException("Supplier", "id", id) }

    @Transactional
    override fun createSupplier(request: CreateSupplierRequest): SupplierDto {
        // Validate unique constraints
        if (!request.name.isNullOrBlank() && supplierRepository.existsByName(request.name)) {
            throw IllegalArgumentException("Supplier with name '${request.name}' already exists")
        }
        if (!request.email.isNullOrBlank() && supplierRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Supplier with email '${request.email}' already exists")
        }

        // Fetch country if provided
        val country =
            request.countryId?.let { countryId ->
                @Suppress("DEPRECATION")
                countryService.getCountryEntityReference(countryId)
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
        val existingSupplier =
            supplierRepository
                .findById(id)
                .orElseThrow { SupplierNotFoundException("Supplier", "id", id) }

        val oldDto = existingSupplier.toDto()

        // Validate unique constraints
        if (!request.name.isNullOrBlank() && supplierRepository.existsByNameAndIdNot(request.name, id)) {
            throw IllegalArgumentException("Supplier with name '${request.name}' already exists")
        }
        if (!request.email.isNullOrBlank() && supplierRepository.existsByEmailAndIdNot(request.email, id)) {
            throw IllegalArgumentException("Supplier with email '${request.email}' already exists")
        }

        // Fetch country if provided
        val country =
            request.countryId?.let { countryId ->
                @Suppress("DEPRECATION")
                countryService.getCountryEntityReference(countryId)
            }

        val updatedSupplier =
            Supplier(
                id = existingSupplier.id,
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
                createdAt = existingSupplier.createdAt,
                updatedAt = LocalDateTime.now(),
            )

        val savedSupplier = supplierRepository.save(updatedSupplier)
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

    private fun Supplier.toDto(): SupplierDto =
        SupplierDto(
            id = id!!,
            name = name,
            title = title,
            firstName = firstName,
            lastName = lastName,
            street = street,
            houseNumber = houseNumber,
            city = city,
            postalCode = postalCode,
            country = country?.toDto(),
            phoneNumber1 = phoneNumber1,
            phoneNumber2 = phoneNumber2,
            phoneNumber3 = phoneNumber3,
            email = email,
            website = website,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}
