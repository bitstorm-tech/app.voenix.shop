package com.jotoai.voenix.shop.prompts.service

import com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.images.dto.ImageType
import com.jotoai.voenix.shop.images.service.ImageService
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
    private val imageService: ImageService,
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
                description = request.description,
                exampleImageFilename = request.exampleImageFilename,
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

        request.description?.let { newDescription ->
            slot.description = newDescription
        }

        // Handle example image filename change
        request.exampleImageFilename?.let { newFilename ->
            val oldFilename = slot.exampleImageFilename
            if (oldFilename != null && oldFilename != newFilename) {
                // Delete old image if filename changed
                try {
                    imageService.delete(oldFilename, ImageType.SLOT_EXAMPLE)
                } catch (e: Exception) {
                    // Log but don't fail if old image doesn't exist
                }
            }
            slot.exampleImageFilename = newFilename
        }

        val updatedSlot = slotRepository.save(slot)

        // Load the slot type for the response
        updatedSlot.slotType = slotTypeRepository.findById(updatedSlot.slotTypeId).orElse(null)

        return updatedSlot.toDto()
    }

    @Transactional
    fun deleteSlot(id: Long) {
        val slot =
            slotRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("Slot", "id", id) }

        // Delete associated image if exists
        slot.exampleImageFilename?.let { filename ->
            try {
                imageService.delete(filename, ImageType.SLOT_EXAMPLE)
            } catch (e: Exception) {
                // Log but don't fail if image doesn't exist
            }
        }

        slotRepository.deleteById(id)
    }
}
