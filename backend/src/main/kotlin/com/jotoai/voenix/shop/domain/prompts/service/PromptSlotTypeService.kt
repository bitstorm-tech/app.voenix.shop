package com.jotoai.voenix.shop.domain.prompts.service

import com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.prompts.dto.CreatePromptSlotTypeRequest
import com.jotoai.voenix.shop.domain.prompts.dto.PromptSlotTypeDto
import com.jotoai.voenix.shop.domain.prompts.dto.UpdatePromptSlotTypeRequest
import com.jotoai.voenix.shop.domain.prompts.entity.PromptSlotType
import com.jotoai.voenix.shop.domain.prompts.repository.PromptSlotTypeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptSlotTypeService(
    private val promptSlotTypeRepository: PromptSlotTypeRepository,
) {
    fun getAllPromptSlotTypes(): List<PromptSlotTypeDto> = promptSlotTypeRepository.findAll().map { it.toDto() }

    fun getPromptSlotTypeById(id: Long): PromptSlotTypeDto =
        promptSlotTypeRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { ResourceNotFoundException("PromptSlotType", "id", id) }

    @Transactional
    fun createPromptSlotType(request: CreatePromptSlotTypeRequest): PromptSlotTypeDto {
        if (promptSlotTypeRepository.existsByName(request.name)) {
            throw ResourceAlreadyExistsException("PromptSlotType", "name", request.name)
        }

        if (promptSlotTypeRepository.existsByPosition(request.position)) {
            throw ResourceAlreadyExistsException("PromptSlotType", "position", request.position)
        }

        val promptSlotType = PromptSlotType(name = request.name, position = request.position)
        val savedPromptSlotType = promptSlotTypeRepository.save(promptSlotType)

        return savedPromptSlotType.toDto()
    }

    @Transactional
    fun updatePromptSlotType(
        id: Long,
        request: UpdatePromptSlotTypeRequest,
    ): PromptSlotTypeDto {
        val promptSlotType =
            promptSlotTypeRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("PromptSlotType", "id", id) }

        request.name?.let { newName ->
            if (promptSlotTypeRepository.existsByNameAndIdNot(newName, id)) {
                throw ResourceAlreadyExistsException("PromptSlotType", "name", newName)
            }
            promptSlotType.name = newName
        }

        request.position?.let { newPosition ->
            if (promptSlotTypeRepository.existsByPositionAndIdNot(newPosition, id)) {
                throw ResourceAlreadyExistsException("PromptSlotType", "position", newPosition)
            }
            promptSlotType.position = newPosition
        }

        val updatedPromptSlotType = promptSlotTypeRepository.save(promptSlotType)
        return updatedPromptSlotType.toDto()
    }

    @Transactional
    fun deletePromptSlotType(id: Long) {
        if (!promptSlotTypeRepository.existsById(id)) {
            throw ResourceNotFoundException("PromptSlotType", "id", id)
        }
        promptSlotTypeRepository.deleteById(id)
    }
}
