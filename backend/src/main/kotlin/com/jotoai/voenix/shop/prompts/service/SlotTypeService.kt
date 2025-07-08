package com.jotoai.voenix.shop.prompts.service

import com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.prompts.dto.CreateSlotTypeRequest
import com.jotoai.voenix.shop.prompts.dto.SlotTypeDto
import com.jotoai.voenix.shop.prompts.dto.UpdateSlotTypeRequest
import com.jotoai.voenix.shop.prompts.entity.SlotType
import com.jotoai.voenix.shop.prompts.repository.SlotTypeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SlotTypeService(
    private val slotTypeRepository: SlotTypeRepository,
) {
    fun getAllSlotTypes(): List<SlotTypeDto> = slotTypeRepository.findAll().map { it.toDto() }

    fun getSlotTypeById(id: Long): SlotTypeDto =
        slotTypeRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { ResourceNotFoundException("SlotType", "id", id) }

    @Transactional
    fun createSlotType(request: CreateSlotTypeRequest): SlotTypeDto {
        if (slotTypeRepository.existsByName(request.name)) {
            throw ResourceAlreadyExistsException("SlotType", "name", request.name)
        }

        val slotType = SlotType(name = request.name)
        val savedSlotType = slotTypeRepository.save(slotType)

        return savedSlotType.toDto()
    }

    @Transactional
    fun updateSlotType(
        id: Long,
        request: UpdateSlotTypeRequest,
    ): SlotTypeDto {
        val slotType =
            slotTypeRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("SlotType", "id", id) }

        request.name?.let { newName ->
            if (slotTypeRepository.existsByNameAndIdNot(newName, id)) {
                throw ResourceAlreadyExistsException("SlotType", "name", newName)
            }
            slotType.name = newName
        }

        val updatedSlotType = slotTypeRepository.save(slotType)
        return updatedSlotType.toDto()
    }

    @Transactional
    fun deleteSlotType(id: Long) {
        if (!slotTypeRepository.existsById(id)) {
            throw ResourceNotFoundException("SlotType", "id", id)
        }
        slotTypeRepository.deleteById(id)
    }
}
