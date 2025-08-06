package com.jotoai.voenix.shop.vat.internal.service

import com.jotoai.voenix.shop.vat.api.VatFacade
import com.jotoai.voenix.shop.vat.api.VatQueryService
import com.jotoai.voenix.shop.vat.api.dto.CreateValueAddedTaxRequest
import com.jotoai.voenix.shop.vat.api.dto.UpdateValueAddedTaxRequest
import com.jotoai.voenix.shop.vat.api.dto.ValueAddedTaxDto
import com.jotoai.voenix.shop.vat.api.exception.VatNotFoundException
import com.jotoai.voenix.shop.vat.events.DefaultVatChangedEvent
import com.jotoai.voenix.shop.vat.events.VatCreatedEvent
import com.jotoai.voenix.shop.vat.events.VatDeletedEvent
import com.jotoai.voenix.shop.vat.events.VatUpdatedEvent
import com.jotoai.voenix.shop.vat.internal.entity.ValueAddedTax
import com.jotoai.voenix.shop.vat.internal.repository.ValueAddedTaxRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class VatServiceImpl(
    private val valueAddedTaxRepository: ValueAddedTaxRepository,
    private val eventPublisher: ApplicationEventPublisher,
) : VatFacade,
    VatQueryService {
    override fun getAllVats(): List<ValueAddedTaxDto> = valueAddedTaxRepository.findAll().map { it.toDto() }

    override fun getDefaultVat(): ValueAddedTaxDto? = valueAddedTaxRepository.findByIsDefaultTrue().map { it.toDto() }.orElse(null)

    override fun getVatById(id: Long): ValueAddedTaxDto =
        valueAddedTaxRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { VatNotFoundException("ValueAddedTax", "id", id) }

    override fun existsById(id: Long): Boolean = valueAddedTaxRepository.existsById(id)

    @Transactional
    override fun createVat(request: CreateValueAddedTaxRequest): ValueAddedTaxDto {
        if (valueAddedTaxRepository.existsByName(request.name)) {
            throw IllegalArgumentException("VAT with name '${request.name}' already exists")
        }

        val previousDefaultId =
            if (request.isDefault == true) {
                val previousDefault = valueAddedTaxRepository.findByIsDefaultTrue().orElse(null)
                valueAddedTaxRepository.clearDefaultExcept(0) // 0 means clear all defaults
                previousDefault?.id
            } else {
                null
            }

        val vat =
            ValueAddedTax(
                name = request.name,
                percent = request.percent,
                description = request.description,
                isDefault = request.isDefault ?: false,
            )

        val savedVat = valueAddedTaxRepository.save(vat)
        val result = savedVat.toDto()

        // Publish events
        eventPublisher.publishEvent(VatCreatedEvent(result))
        if (request.isDefault == true) {
            eventPublisher.publishEvent(DefaultVatChangedEvent(previousDefaultId, result.id))
        }

        return result
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

        val oldDto = vat.toDto()
        var defaultChanged = false
        var previousDefaultId: Long? = null

        request.name?.let { newName ->
            if (valueAddedTaxRepository.existsByNameAndIdNot(newName, id)) {
                throw IllegalArgumentException("VAT with name '$newName' already exists")
            }
            vat.name = newName
        }

        request.percent?.let { vat.percent = it }
        request.description?.let { vat.description = it }
        request.isDefault?.let { isDefault ->
            if (isDefault && !vat.isDefault) {
                // If setting this VAT as default, unset any other defaults
                val previousDefault = valueAddedTaxRepository.findByIsDefaultTrue().orElse(null)
                previousDefaultId = previousDefault?.id
                valueAddedTaxRepository.clearDefaultExcept(id)
                defaultChanged = true
            } else if (!isDefault && vat.isDefault) {
                // Unsetting default
                defaultChanged = true
            }
            vat.isDefault = isDefault
        }

        val updatedVat = valueAddedTaxRepository.save(vat)
        val result = updatedVat.toDto()

        // Publish events
        eventPublisher.publishEvent(VatUpdatedEvent(oldDto, result))
        if (defaultChanged) {
            eventPublisher.publishEvent(DefaultVatChangedEvent(previousDefaultId, if (vat.isDefault) id else null))
        }

        return result
    }

    @Transactional
    override fun deleteVat(id: Long) {
        val vat =
            valueAddedTaxRepository
                .findById(id)
                .orElseThrow { VatNotFoundException("ValueAddedTax", "id", id) }

        val vatDto = vat.toDto()
        val wasDefault = vat.isDefault

        valueAddedTaxRepository.deleteById(id)

        // Publish events
        eventPublisher.publishEvent(VatDeletedEvent(vatDto))
        if (wasDefault) {
            eventPublisher.publishEvent(DefaultVatChangedEvent(id, null))
        }
    }
}
