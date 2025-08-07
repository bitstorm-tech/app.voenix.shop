package com.jotoai.voenix.shop.domain.prompts.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.image.api.ImageFacade
import com.jotoai.voenix.shop.domain.prompts.assembler.PromptAssembler
import com.jotoai.voenix.shop.domain.prompts.dto.AddSlotVariantsRequest
import com.jotoai.voenix.shop.domain.prompts.dto.CreatePromptRequest
import com.jotoai.voenix.shop.domain.prompts.dto.PromptDto
import com.jotoai.voenix.shop.domain.prompts.dto.UpdatePromptRequest
import com.jotoai.voenix.shop.domain.prompts.dto.UpdatePromptSlotVariantsRequest
import com.jotoai.voenix.shop.domain.prompts.entity.Prompt
import com.jotoai.voenix.shop.domain.prompts.repository.PromptCategoryRepository
import com.jotoai.voenix.shop.domain.prompts.repository.PromptRepository
import com.jotoai.voenix.shop.domain.prompts.repository.PromptSlotVariantRepository
import com.jotoai.voenix.shop.domain.prompts.repository.PromptSubCategoryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptService(
    private val promptRepository: PromptRepository,
    private val promptCategoryRepository: PromptCategoryRepository,
    private val promptSubCategoryRepository: PromptSubCategoryRepository,
    private val promptSlotVariantRepository: PromptSlotVariantRepository,
    private val imageService: ImageService,
    private val promptAssembler: PromptAssembler,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PromptService::class.java)
    }

    fun getAllPrompts(): List<PromptDto> =
        promptRepository.findAllWithRelations().map { prompt ->
            promptAssembler.toDto(prompt)
        }

    fun getPromptById(id: Long): PromptDto =
        promptRepository
            .findByIdWithRelations(id)
            .map { prompt ->
                promptAssembler.toDto(prompt)
            }.orElseThrow { ResourceNotFoundException("Prompt", "id", id) }

    fun searchPromptsByTitle(title: String): List<PromptDto> =
        promptRepository.findByTitleContainingIgnoreCase(title).map {
            promptAssembler.toDto(it)
        }

    @Transactional
    fun createPrompt(request: CreatePromptRequest): PromptDto {
        // Validate category exists if provided
        if (request.categoryId != null && !promptCategoryRepository.existsById(request.categoryId)) {
            throw ResourceNotFoundException("PromptCategory", "id", request.categoryId)
        }

        // Validate subcategory exists if provided
        if (request.subcategoryId != null && !promptSubCategoryRepository.existsById(request.subcategoryId)) {
            throw ResourceNotFoundException("PromptSubCategory", "id", request.subcategoryId)
        }

        val prompt =
            Prompt(
                title = request.title,
                promptText = request.promptText,
                categoryId = request.categoryId,
                subcategoryId = request.subcategoryId,
                exampleImageFilename = request.exampleImageFilename,
            )

        // Add slot variants if provided
        if (request.slots.isNotEmpty()) {
            request.slots.forEach { slotVariantRequest ->
                val slotVariant =
                    promptSlotVariantRepository
                        .findById(slotVariantRequest.slotId)
                        .orElseThrow { ResourceNotFoundException("Slot variant", "id", slotVariantRequest.slotId) }
                prompt.addPromptSlotVariant(slotVariant)
            }
        }

        val savedPrompt = promptRepository.save(prompt)

        // Reload with relations for response
        return getPromptById(savedPrompt.id!!)
    }

    @Transactional
    fun updatePrompt(
        id: Long,
        request: UpdatePromptRequest,
    ): PromptDto {
        val prompt =
            promptRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("Prompt", "id", id) }

        // Validate category exists if provided
        if (request.categoryId != null && !promptCategoryRepository.existsById(request.categoryId)) {
            throw ResourceNotFoundException("PromptCategory", "id", request.categoryId)
        }

        // Validate subcategory exists if provided
        if (request.subcategoryId != null && !promptSubCategoryRepository.existsById(request.subcategoryId)) {
            throw ResourceNotFoundException("PromptSubCategory", "id", request.subcategoryId)
        }

        request.title?.let { prompt.title = it }
        request.promptText?.let { prompt.promptText = it }
        request.categoryId?.let { prompt.categoryId = it }
        request.subcategoryId?.let { prompt.subcategoryId = it }
        request.active?.let { prompt.active = it }

        // Handle example image filename change
        request.exampleImageFilename?.let { newFilename ->
            val oldFilename = prompt.exampleImageFilename
            if (oldFilename != null && oldFilename != newFilename) {
                // Delete old image if filename changed
                try {
                    imageService.delete(oldFilename, ImageType.PROMPT_EXAMPLE)
                } catch (e: Exception) {
                    logger.warn("Failed to delete old prompt example image: $oldFilename", e)
                }
            }
            prompt.exampleImageFilename = newFilename
        }

        // Update slot variants if provided
        request.slots?.let { slotVariants ->
            prompt.clearPromptSlotVariants()
            slotVariants.forEach { slotVariantRequest ->
                val promptSlotVariant =
                    promptSlotVariantRepository
                        .findById(slotVariantRequest.slotId)
                        .orElseThrow { ResourceNotFoundException("Prompt slot variant", "id", slotVariantRequest.slotId) }
                prompt.addPromptSlotVariant(promptSlotVariant)
            }
        }

        val updatedPrompt = promptRepository.save(prompt)

        // Reload with relations for response
        return getPromptById(updatedPrompt.id!!)
    }

    @Transactional
    fun deletePrompt(id: Long) {
        val prompt =
            promptRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("Prompt", "id", id) }

        // Delete associated image if exists
        prompt.exampleImageFilename?.let { filename ->
            try {
                imageService.delete(filename, ImageType.PROMPT_EXAMPLE)
            } catch (e: Exception) {
                logger.warn("Failed to delete prompt example image during prompt deletion: $filename", e)
            }
        }

        promptRepository.deleteById(id)
    }

    @Transactional
    fun addSlotVariantsToPrompt(
        promptId: Long,
        request: AddSlotVariantsRequest,
    ): PromptDto {
        val prompt =
            promptRepository
                .findById(promptId)
                .orElseThrow { ResourceNotFoundException("Prompt", "id", promptId) }

        val promptSlotVariants = promptSlotVariantRepository.findAllById(request.slotIds)
        if (promptSlotVariants.size != request.slotIds.size) {
            val foundIds = promptSlotVariants.mapNotNull { it.id }
            val missingIds = request.slotIds - foundIds.toSet()
            throw ResourceNotFoundException("Prompt slot variant", "ids", missingIds.joinToString(", "))
        }

        promptSlotVariants.forEach { promptSlotVariant ->
            prompt.addPromptSlotVariant(promptSlotVariant)
        }

        val savedPrompt = promptRepository.save(prompt)
        return getPromptById(savedPrompt.id!!)
    }

    @Transactional
    fun updatePromptSlotVariants(
        promptId: Long,
        request: UpdatePromptSlotVariantsRequest,
    ): PromptDto {
        val prompt =
            promptRepository
                .findById(promptId)
                .orElseThrow { ResourceNotFoundException("Prompt", "id", promptId) }

        // Clear existing slot variants
        prompt.clearPromptSlotVariants()

        // Add new slot variants
        request.slotVariants.forEach { slotVariantRequest ->
            val promptSlotVariant =
                promptSlotVariantRepository
                    .findById(slotVariantRequest.slotId)
                    .orElseThrow { ResourceNotFoundException("Prompt slot variant", "id", slotVariantRequest.slotId) }
            prompt.addPromptSlotVariant(promptSlotVariant)
        }

        val savedPrompt = promptRepository.save(prompt)
        return getPromptById(savedPrompt.id!!)
    }

    @Transactional
    fun removeSlotVariantFromPrompt(
        promptId: Long,
        slotId: Long,
    ): PromptDto {
        val prompt =
            promptRepository
                .findById(promptId)
                .orElseThrow { ResourceNotFoundException("Prompt", "id", promptId) }

        val promptSlotVariant =
            promptSlotVariantRepository
                .findById(slotId)
                .orElseThrow { ResourceNotFoundException("Prompt slot variant", "id", slotId) }

        prompt.removePromptSlotVariant(promptSlotVariant)

        val savedPrompt = promptRepository.save(prompt)
        return getPromptById(savedPrompt.id!!)
    }
}
