package com.jotoai.voenix.shop.domain.vat.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.vat.dto.CreateValueAddedTaxRequest
import com.jotoai.voenix.shop.domain.vat.dto.UpdateValueAddedTaxRequest
import com.jotoai.voenix.shop.domain.vat.dto.ValueAddedTaxDto
import com.jotoai.voenix.shop.domain.vat.entity.ValueAddedTax
import com.jotoai.voenix.shop.domain.vat.repository.ValueAddedTaxRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ValueAddedTaxService(
    private val valueAddedTaxRepository: ValueAddedTaxRepository,
) {
    fun getAllVats(): List<ValueAddedTaxDto> = valueAddedTaxRepository.findAll().map { it.toDto() }

    fun getDefaultVat(): ValueAddedTaxDto? = valueAddedTaxRepository.findByIsDefaultTrue().map { it.toDto() }.orElse(null)

    fun getVatById(id: Long): ValueAddedTaxDto =
        valueAddedTaxRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { ResourceNotFoundException("ValueAddedTax", "id", id) }

    @Transactional
    fun createVat(request: CreateValueAddedTaxRequest): ValueAddedTaxDto {
        if (valueAddedTaxRepository.existsByName(request.name)) {
            throw IllegalArgumentException("VAT with name '${request.name}' already exists")
        }

        // If this VAT is marked as default, unset any existing default
        if (request.isDefault == true) {
            valueAddedTaxRepository.clearDefaultExcept(0) // 0 means clear all defaults
        }

        val vat =
            ValueAddedTax(
                name = request.name,
                percent = request.percent,
                description = request.description,
                isDefault = request.isDefault ?: false,
            )

        val savedVat = valueAddedTaxRepository.save(vat)
        return savedVat.toDto()
    }

    @Transactional
    fun updateVat(
        id: Long,
        request: UpdateValueAddedTaxRequest,
    ): ValueAddedTaxDto {
        val vat =
            valueAddedTaxRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("ValueAddedTax", "id", id) }

        request.name?.let { newName ->
            if (valueAddedTaxRepository.existsByNameAndIdNot(newName, id)) {
                throw IllegalArgumentException("VAT with name '$newName' already exists")
            }
            vat.name = newName
        }

        request.percent?.let { vat.percent = it }
        request.description?.let { vat.description = it }
        request.isDefault?.let { isDefault ->
            if (isDefault) {
                // If setting this VAT as default, unset any other defaults
                valueAddedTaxRepository.clearDefaultExcept(id)
            }
            vat.isDefault = isDefault
        }

        val updatedVat = valueAddedTaxRepository.save(vat)
        return updatedVat.toDto()
    }

    @Transactional
    fun deleteVat(id: Long) {
        val vat =
            valueAddedTaxRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("ValueAddedTax", "id", id) }

        // Check if this is the default VAT being deleted
        if (vat.isDefault) {
            // Log warning or notify that default VAT is being deleted
            // After deletion, there will be no default VAT
        }

        valueAddedTaxRepository.deleteById(id)
    }
}
