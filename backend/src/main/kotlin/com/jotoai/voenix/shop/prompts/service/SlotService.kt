package com.jotoai.voenix.shop.prompts.service

import com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.prompts.dto.CreateSlotRequest
import com.jotoai.voenix.shop.prompts.dto.SlotDto
import com.jotoai.voenix.shop.prompts.dto.UpdateSlotRequest
import com.jotoai.voenix.shop.prompts.entity.Slot
import com.jotoai.voenix.shop.prompts.repository.SlotRepository
import com.jotoai.voenix.shop.prompts.repository.SlotTypeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SlotService(
    private val slotRepository: SlotRepository,
    private val slotTypeRepository: SlotTypeRepository,
) {
    fun getAllSlots(): List<SlotDto> = slotRepository.findAll().map { it.toDto() }

    fun getSlotById(id: Long): SlotDto =
        slotRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { ResourceNotFoundException("Slot", "id", id) }

    fun getSlotsBySlotType(slotTypeId: Long): List<SlotDto> {
        if (!slotTypeRepository.existsById(slotTypeId)) {
            throw ResourceNotFoundException("SlotType", "id", slotTypeId)
        }
        return slotRepository.findBySlotTypeId(slotTypeId).map { it.toDto() }
    }

    @Transactional
    fun createSlot(request: CreateSlotRequest): SlotDto {
        if (!slotTypeRepository.existsById(request.slotTypeId)) {
            throw ResourceNotFoundException("SlotType", "id", request.slotTypeId)
        }

        if (slotRepository.existsByName(request.name)) {
            throw ResourceAlreadyExistsException("Slot", "name", request.name)
        }

        val slot =
            Slot(
                slotTypeId = request.slotTypeId,
                name = request.name,
                prompt = request.prompt,
            )
        val savedSlot = slotRepository.save(slot)

        // Load the slot type for the response
        savedSlot.slotType = slotTypeRepository.findById(request.slotTypeId).orElse(null)

        return savedSlot.toDto()
    }

    @Transactional
    fun updateSlot(
        id: Long,
        request: UpdateSlotRequest,
    ): SlotDto {
        val slot =
            slotRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("Slot", "id", id) }

        request.slotTypeId?.let { newSlotTypeId ->
            if (!slotTypeRepository.existsById(newSlotTypeId)) {
                throw ResourceNotFoundException("SlotType", "id", newSlotTypeId)
            }
            slot.slotTypeId = newSlotTypeId
        }

        request.name?.let { newName ->
            if (slotRepository.existsByNameAndIdNot(newName, id)) {
                throw ResourceAlreadyExistsException("Slot", "name", newName)
            }
            slot.name = newName
        }

        request.prompt?.let { newPrompt ->
            slot.prompt = newPrompt
        }

        val updatedSlot = slotRepository.save(slot)

        // Load the slot type for the response
        updatedSlot.slotType = slotTypeRepository.findById(updatedSlot.slotTypeId).orElse(null)

        return updatedSlot.toDto()
    }

    @Transactional
    fun deleteSlot(id: Long) {
        if (!slotRepository.existsById(id)) {
            throw ResourceNotFoundException("Slot", "id", id)
        }
        slotRepository.deleteById(id)
    }
}
