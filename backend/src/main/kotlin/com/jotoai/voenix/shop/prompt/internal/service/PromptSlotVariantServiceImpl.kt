package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.prompt.api.PromptSlotVariantFacade
import com.jotoai.voenix.shop.prompt.api.PromptSlotVariantQueryService
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.CreatePromptSlotVariantRequest
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.PromptSlotVariantDto
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.UpdatePromptSlotVariantRequest
import com.jotoai.voenix.shop.prompt.api.exceptions.PromptSlotVariantNotFoundException
import com.jotoai.voenix.shop.prompt.internal.entity.PromptSlotVariant
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSlotTypeRepository
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSlotVariantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptSlotVariantServiceImpl(
    private val promptSlotVariantRepository: PromptSlotVariantRepository,
    private val promptSlotTypeRepository: PromptSlotTypeRepository,
    private val promptSlotVariantAssembler: PromptSlotVariantAssembler,
) : PromptSlotVariantFacade,
    PromptSlotVariantQueryService {
    override fun getAllSlotVariants(): List<PromptSlotVariantDto> =
        promptSlotVariantRepository.findAll().map { promptSlotVariantAssembler.toDto(it) }

    override fun getSlotVariantById(id: Long): PromptSlotVariantDto =
        promptSlotVariantRepository
            .findById(id)
            .map { promptSlotVariantAssembler.toDto(it) }
            .orElseThrow { PromptSlotVariantNotFoundException("Prompt slot variant", "id", id) }

    override fun getSlotVariantsBySlotType(promptSlotTypeId: Long): List<PromptSlotVariantDto> {
        if (!promptSlotTypeRepository.existsById(promptSlotTypeId)) {
            throw PromptSlotVariantNotFoundException("PromptSlotType", "id", promptSlotTypeId)
        }
        return promptSlotVariantRepository.findByPromptSlotTypeId(promptSlotTypeId).map {
            promptSlotVariantAssembler.toDto(it)
        }
    }

    override fun existsById(id: Long): Boolean = promptSlotVariantRepository.existsById(id)

    @Transactional
    override fun createSlotVariant(request: CreatePromptSlotVariantRequest): PromptSlotVariantDto {
        // Validate that promptSlotTypeId exists
        require(promptSlotTypeRepository.existsById(request.promptSlotTypeId)) {
            "PromptSlotType with id '${request.promptSlotTypeId}' does not exist"
        }
        
        // Check name uniqueness
        require(!promptSlotVariantRepository.existsByName(request.name)) {
            "PromptSlotVariant with name '${request.name}' already exists"
        }
        
        // Create new PromptSlotVariant entity
        val promptSlotVariant = PromptSlotVariant(
            promptSlotTypeId = request.promptSlotTypeId,
            name = request.name,
            prompt = request.prompt,
            description = request.description,
            exampleImageFilename = request.exampleImageFilename
        )
        
        // Save to repository and return DTO
        val savedEntity = promptSlotVariantRepository.save(promptSlotVariant)
        return promptSlotVariantAssembler.toDto(savedEntity)
    }

    @Transactional
    override fun updateSlotVariant(
        id: Long,
        request: UpdatePromptSlotVariantRequest,
    ): PromptSlotVariantDto {
        // Find existing entity or throw exception
        val promptSlotVariant = promptSlotVariantRepository
            .findById(id)
            .orElseThrow { PromptSlotVariantNotFoundException("PromptSlotVariant", "id", id) }
        
        // If promptSlotTypeId is provided, validate it exists
        request.promptSlotTypeId?.let { newPromptSlotTypeId ->
            require(promptSlotTypeRepository.existsById(newPromptSlotTypeId)) {
                "PromptSlotType with id '$newPromptSlotTypeId' does not exist"
            }
            promptSlotVariant.promptSlotTypeId = newPromptSlotTypeId
        }
        
        // If name is changed, validate uniqueness (excluding current entity)
        request.name?.let { newName ->
            require(!promptSlotVariantRepository.existsByNameAndIdNot(newName, id)) {
                "PromptSlotVariant with name '$newName' already exists"
            }
            promptSlotVariant.name = newName
        }
        
        // Update all other provided fields
        request.prompt?.let { promptSlotVariant.prompt = it }
        request.description?.let { promptSlotVariant.description = it }
        request.exampleImageFilename?.let { promptSlotVariant.exampleImageFilename = it }
        
        // Save and return DTO
        val savedEntity = promptSlotVariantRepository.save(promptSlotVariant)
        return promptSlotVariantAssembler.toDto(savedEntity)
    }

    @Transactional
    override fun deleteSlotVariant(id: Long) {
        // Check entity exists before deletion
        if (!promptSlotVariantRepository.existsById(id)) {
            throw PromptSlotVariantNotFoundException("PromptSlotVariant", "id", id)
        }
        
        // Delete by ID
        promptSlotVariantRepository.deleteById(id)
    }
}
