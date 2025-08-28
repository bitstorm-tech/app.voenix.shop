package com.jotoai.voenix.shop.vat.internal.service

import com.jotoai.voenix.shop.vat.api.VatService
import com.jotoai.voenix.shop.vat.api.dto.CreateValueAddedTaxRequest
import com.jotoai.voenix.shop.vat.api.dto.UpdateValueAddedTaxRequest
import com.jotoai.voenix.shop.vat.api.dto.ValueAddedTaxDto
import com.jotoai.voenix.shop.vat.api.exception.VatNotFoundException
import com.jotoai.voenix.shop.vat.internal.entity.ValueAddedTax
import com.jotoai.voenix.shop.vat.internal.repository.ValueAddedTaxRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class VatServiceImpl(
    private val valueAddedTaxRepository: ValueAddedTaxRepository,
) : VatService {
    override fun getAllVats(): List<ValueAddedTaxDto> = valueAddedTaxRepository.findAll().map { it.toDto() }

    override fun getDefaultVat(): ValueAddedTaxDto? =
        valueAddedTaxRepository
            .findByIsDefaultTrue()
            .map { it.toDto() }
            .orElse(null)

    override fun getVatById(id: Long): ValueAddedTaxDto =
        valueAddedTaxRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { VatNotFoundException("ValueAddedTax", "id", id) }

    override fun existsById(id: Long): Boolean = valueAddedTaxRepository.existsById(id)

    @Transactional
    override fun createVat(request: CreateValueAddedTaxRequest): ValueAddedTaxDto {
        require(!valueAddedTaxRepository.existsByName(request.name)) {
            "VAT with name '${request.name}' already exists"
        }

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
    override fun updateVat(
        id: Long,
        request: UpdateValueAddedTaxRequest,
    ): ValueAddedTaxDto {
        val vat =
            valueAddedTaxRepository
                .findById(id)
                .orElseThrow { VatNotFoundException("ValueAddedTax", "id", id) }

        request.name?.let { newName ->
            require(!valueAddedTaxRepository.existsByNameAndIdNot(newName, id)) {
                "VAT with name '$newName' already exists"
            }
            vat.name = newName
        }

        request.percent?.let { vat.percent = it }
        request.description?.let { vat.description = it }
        request.isDefault?.let { isDefault ->
            if (isDefault && !vat.isDefault) {
                // If setting this VAT as default, unset any other defaults
                valueAddedTaxRepository.clearDefaultExcept(id)
            }
            vat.isDefault = isDefault
        }

        val updatedVat = valueAddedTaxRepository.save(vat)
        return updatedVat.toDto()
    }

    @Transactional
    override fun deleteVat(id: Long) {
        if (!valueAddedTaxRepository.existsById(id)) {
            throw VatNotFoundException("ValueAddedTax", "id", id)
        }

        valueAddedTaxRepository.deleteById(id)
    }
}
