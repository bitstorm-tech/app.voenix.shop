package com.jotoai.voenix.shop.domain.suppliers.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.countries.repository.CountryRepository
import com.jotoai.voenix.shop.domain.suppliers.dto.CreateSupplierRequest
import com.jotoai.voenix.shop.domain.suppliers.dto.SupplierDto
import com.jotoai.voenix.shop.domain.suppliers.dto.UpdateSupplierRequest
import com.jotoai.voenix.shop.domain.suppliers.entity.Supplier
import com.jotoai.voenix.shop.domain.suppliers.repository.SupplierRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class SupplierService(
    private val supplierRepository: SupplierRepository,
    private val countryRepository: CountryRepository,
) {
    fun getAllSuppliers(): List<SupplierDto> = supplierRepository.findAll().map { it.toDto() }

    fun getSupplierById(id: Long): SupplierDto {
        val supplier =
            supplierRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("Supplier not found with id: $id") }
        return supplier.toDto()
    }

    fun createSupplier(request: CreateSupplierRequest): SupplierDto {
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
                countryRepository
                    .findById(countryId)
                    .orElseThrow { ResourceNotFoundException("Country", "id", countryId) }
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

        return supplierRepository.save(supplier).toDto()
    }

    fun updateSupplier(
        id: Long,
        request: UpdateSupplierRequest,
    ): SupplierDto {
        val existingSupplier =
            supplierRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("Supplier not found with id: $id") }

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
                countryRepository
                    .findById(countryId)
                    .orElseThrow { ResourceNotFoundException("Country", "id", countryId) }
            }

        val updatedSupplier =
            existingSupplier.copy(
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
                updatedAt = LocalDateTime.now(),
            )

        return supplierRepository.save(updatedSupplier).toDto()
    }

    fun deleteSupplier(id: Long) {
        if (!supplierRepository.existsById(id)) {
            throw ResourceNotFoundException("Supplier not found with id: $id")
        }
        supplierRepository.deleteById(id)
    }
}
