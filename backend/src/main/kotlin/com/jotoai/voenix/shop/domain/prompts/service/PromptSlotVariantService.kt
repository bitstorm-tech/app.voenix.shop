package com.jotoai.voenix.shop.domain.prompts.service

import com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.domain.prompts.assembler.PromptSlotVariantAssembler
import com.jotoai.voenix.shop.domain.prompts.dto.CreatePromptSlotVariantRequest
import com.jotoai.voenix.shop.domain.prompts.dto.PromptSlotVariantDto
import com.jotoai.voenix.shop.domain.prompts.dto.UpdatePromptSlotVariantRequest
import com.jotoai.voenix.shop.domain.prompts.entity.PromptSlotVariant
import com.jotoai.voenix.shop.domain.prompts.repository.PromptSlotTypeRepository
import com.jotoai.voenix.shop.domain.prompts.repository.PromptSlotVariantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptSlotVariantService(
    private val promptSlotVariantRepository: PromptSlotVariantRepository,
    private val promptSlotTypeRepository: PromptSlotTypeRepository,
    private val imageService: ImageService,
    private val promptSlotVariantAssembler: PromptSlotVariantAssembler,
) {
    fun getAllSlotVariants(): List<PromptSlotVariantDto> =
        promptSlotVariantRepository.findAll().map { promptSlotVariantAssembler.toDto(it) }

    fun getSlotVariantById(id: Long): PromptSlotVariantDto =
        promptSlotVariantRepository
            .findById(id)
            .map { promptSlotVariantAssembler.toDto(it) }
            .orElseThrow { ResourceNotFoundException("Prompt slot variant", "id", id) }

    fun getSlotVariantsBySlotType(promptSlotTypeId: Long): List<PromptSlotVariantDto> {
        if (!promptSlotTypeRepository.existsById(promptSlotTypeId)) {
            throw ResourceNotFoundException("PromptSlotType", "id", promptSlotTypeId)
        }
        return promptSlotVariantRepository.findByPromptSlotTypeId(promptSlotTypeId).map { promptSlotVariantAssembler.toDto(it) }
    }

    @Transactional
    fun createSlotVariant(request: CreatePromptSlotVariantRequest): PromptSlotVariantDto {
        if (!promptSlotTypeRepository.existsById(request.promptSlotTypeId)) {
            throw ResourceNotFoundException("PromptSlotType", "id", request.promptSlotTypeId)
        }

        if (promptSlotVariantRepository.existsByName(request.name)) {
            throw ResourceAlreadyExistsException("Prompt slot variant", "name", request.name)
        }

        val promptSlotVariant =
            PromptSlotVariant(
                promptSlotTypeId = request.promptSlotTypeId,
                name = request.name,
                prompt = request.prompt,
                description = request.description,
                exampleImageFilename = request.exampleImageFilename,
            )
        val savedPromptSlotVariant = promptSlotVariantRepository.save(promptSlotVariant)

        // Load the slot type for the response
        savedPromptSlotVariant.promptSlotType = promptSlotTypeRepository.findById(request.promptSlotTypeId).orElse(null)

        return promptSlotVariantAssembler.toDto(savedPromptSlotVariant)
    }

    @Transactional
    fun updateSlotVariant(
        id: Long,
        request: UpdatePromptSlotVariantRequest,
    ): PromptSlotVariantDto {
        val promptSlotVariant =
            promptSlotVariantRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("Prompt slot variant", "id", id) }

        request.promptSlotTypeId?.let { newPromptSlotTypeId ->
            if (!promptSlotTypeRepository.existsById(newPromptSlotTypeId)) {
                throw ResourceNotFoundException("PromptSlotType", "id", newPromptSlotTypeId)
            }
            promptSlotVariant.promptSlotTypeId = newPromptSlotTypeId
        }

        request.name?.let { newName ->
            if (promptSlotVariantRepository.existsByNameAndIdNot(newName, id)) {
                throw ResourceAlreadyExistsException("Prompt slot variant", "name", newName)
            }
            promptSlotVariant.name = newName
        }

        request.prompt?.let { newPrompt ->
            promptSlotVariant.prompt = newPrompt
        }

        request.description?.let { newDescription ->
            promptSlotVariant.description = newDescription
        }

        // Handle example image filename change
        request.exampleImageFilename?.let { newFilename ->
            val oldFilename = promptSlotVariant.exampleImageFilename
            if (oldFilename != null && oldFilename != newFilename) {
                // Delete old image if filename changed
                try {
                    imageService.delete(oldFilename, ImageType.PROMPT_SLOT_VARIANT_EXAMPLE)
                } catch (_: Exception) {
                    // Log but don't fail if old image doesn't exist
                }
            }
            promptSlotVariant.exampleImageFilename = newFilename
        }

        val updatedPromptSlotVariant = promptSlotVariantRepository.save(promptSlotVariant)

        // Load the slot type for the response
        updatedPromptSlotVariant.promptSlotType = promptSlotTypeRepository.findById(updatedPromptSlotVariant.promptSlotTypeId).orElse(null)

        return promptSlotVariantAssembler.toDto(updatedPromptSlotVariant)
    }

    @Transactional
    fun deleteSlotVariant(id: Long) {
        val promptSlotVariant =
            promptSlotVariantRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("Prompt slot variant", "id", id) }

        // Delete associated image if exists
        promptSlotVariant.exampleImageFilename?.let { filename ->
            try {
                imageService.delete(filename, ImageType.PROMPT_SLOT_VARIANT_EXAMPLE)
            } catch (_: Exception) {
                // Log but don't fail if image doesn't exist
            }
        }

        promptSlotVariantRepository.deleteById(id)
    }
}
