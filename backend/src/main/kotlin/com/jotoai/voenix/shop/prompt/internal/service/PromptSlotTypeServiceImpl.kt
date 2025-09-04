package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.application.ResourceNotFoundException
import com.jotoai.voenix.shop.prompt.api.dto.slottypes.CreatePromptSlotTypeRequest
import com.jotoai.voenix.shop.prompt.api.dto.slottypes.PromptSlotTypeDto
import com.jotoai.voenix.shop.prompt.api.dto.slottypes.UpdatePromptSlotTypeRequest
import com.jotoai.voenix.shop.prompt.internal.entity.PromptSlotType
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSlotTypeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptSlotTypeServiceImpl(
    private val promptSlotTypeRepository: PromptSlotTypeRepository,
) {
    fun getAllPromptSlotTypes(): List<PromptSlotTypeDto> =
        promptSlotTypeRepository
            .findAll()
            .map { it.toDto() }

    fun getPromptSlotTypeById(id: Long): PromptSlotTypeDto =
        promptSlotTypeRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { ResourceNotFoundException("PromptSlotType", "id", id) }

    fun existsById(id: Long): Boolean = promptSlotTypeRepository.existsById(id)

    @Transactional
    fun createPromptSlotType(request: CreatePromptSlotTypeRequest): PromptSlotTypeDto {
        require(!promptSlotTypeRepository.existsByName(request.name)) {
            "PromptSlotType with name '${request.name}' already exists"
        }

        require(!promptSlotTypeRepository.existsByPosition(request.position)) {
            "PromptSlotType with position '${request.position}' already exists"
        }

        val promptSlotType = PromptSlotType(name = request.name, position = request.position)
        val saved = promptSlotTypeRepository.save(promptSlotType)
        val result = saved.toDto()

        return result
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

        request.name?.let {
            require(!promptSlotTypeRepository.existsByNameAndIdNot(it, id)) {
                "PromptSlotType with name '$it' already exists"
            }
            promptSlotType.name = it
        }
        request.position?.let {
            require(!promptSlotTypeRepository.existsByPositionAndIdNot(it, id)) {
                "PromptSlotType with position '$it' already exists"
            }
            promptSlotType.position = it
        }

        val saved = promptSlotTypeRepository.save(promptSlotType)
        val result = saved.toDto()

        return result
    }

    @Transactional
    fun deletePromptSlotType(id: Long) {
        promptSlotTypeRepository.deleteById(id)
    }
}
