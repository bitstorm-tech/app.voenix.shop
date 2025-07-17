package com.jotoai.voenix.shop.prompts.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.images.dto.ImageType
import com.jotoai.voenix.shop.images.service.ImageService
import com.jotoai.voenix.shop.prompts.dto.AddSlotsRequest
import com.jotoai.voenix.shop.prompts.dto.CreatePromptRequest
import com.jotoai.voenix.shop.prompts.dto.PromptDto
import com.jotoai.voenix.shop.prompts.dto.UpdatePromptRequest
import com.jotoai.voenix.shop.prompts.dto.UpdatePromptSlotsRequest
import com.jotoai.voenix.shop.prompts.entity.Prompt
import com.jotoai.voenix.shop.prompts.repository.PromptCategoryRepository
import com.jotoai.voenix.shop.prompts.repository.PromptRepository
import com.jotoai.voenix.shop.prompts.repository.PromptSubCategoryRepository
import com.jotoai.voenix.shop.prompts.repository.SlotRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptService(
    private val promptRepository: PromptRepository,
    private val promptCategoryRepository: PromptCategoryRepository,
    private val promptSubCategoryRepository: PromptSubCategoryRepository,
    private val slotRepository: SlotRepository,
    private val imageService: ImageService,
) {
    fun getAllPrompts(): List<PromptDto> =
        promptRepository.findAll().map { prompt ->
            if (prompt.categoryId != null) {
                prompt.category = promptCategoryRepository.findById(prompt.categoryId!!).orElse(null)
            }
            if (prompt.subcategoryId != null) {
                prompt.subcategory = promptSubCategoryRepository.findById(prompt.subcategoryId!!).orElse(null)
            }
            prompt.toDto()
        }

    fun getPromptById(id: Long): PromptDto =
        promptRepository
            .findById(id)
            .map { prompt ->
                if (prompt.categoryId != null) {
                    prompt.category = promptCategoryRepository.findById(prompt.categoryId!!).orElse(null)
                }
                if (prompt.subcategoryId != null) {
                    prompt.subcategory = promptSubCategoryRepository.findById(prompt.subcategoryId!!).orElse(null)
                }
                prompt.toDto()
            }.orElseThrow { ResourceNotFoundException("Prompt", "id", id) }

    fun searchPromptsByTitle(title: String): List<PromptDto> = promptRepository.findByTitleContainingIgnoreCase(title).map { it.toDto() }

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

        // Add slots if provided
        if (request.slots.isNotEmpty()) {
            request.slots.forEach { slotRequest ->
                val slot =
                    slotRepository
                        .findById(slotRequest.slotId)
                        .orElseThrow { ResourceNotFoundException("Slot", "id", slotRequest.slotId) }
                prompt.addSlot(slot)
            }
        }

        val savedPrompt = promptRepository.save(prompt)

        // Load category for response
        if (savedPrompt.categoryId != null) {
            savedPrompt.category = promptCategoryRepository.findById(savedPrompt.categoryId!!).orElse(null)
        }
        // Load subcategory for response
        if (savedPrompt.subcategoryId != null) {
            savedPrompt.subcategory = promptSubCategoryRepository.findById(savedPrompt.subcategoryId!!).orElse(null)
        }

        return savedPrompt.toDto()
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
                    // Log but don't fail if old image doesn't exist
                }
            }
            prompt.exampleImageFilename = newFilename
        }

        // Update slots if provided
        request.slots?.let { slots ->
            prompt.clearSlots()
            slots.forEach { slotRequest ->
                val slot =
                    slotRepository
                        .findById(slotRequest.slotId)
                        .orElseThrow { ResourceNotFoundException("Slot", "id", slotRequest.slotId) }
                prompt.addSlot(slot)
            }
        }

        val updatedPrompt = promptRepository.save(prompt)

        // Load category for response
        if (updatedPrompt.categoryId != null) {
            updatedPrompt.category = promptCategoryRepository.findById(updatedPrompt.categoryId!!).orElse(null)
        }
        // Load subcategory for response
        if (updatedPrompt.subcategoryId != null) {
            updatedPrompt.subcategory = promptSubCategoryRepository.findById(updatedPrompt.subcategoryId!!).orElse(null)
        }

        return updatedPrompt.toDto()
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
                // Log but don't fail if image doesn't exist
            }
        }

        promptRepository.deleteById(id)
    }

    @Transactional
    fun addSlotsToPrompt(
        promptId: Long,
        request: AddSlotsRequest,
    ): PromptDto {
        val prompt =
            promptRepository
                .findById(promptId)
                .orElseThrow { ResourceNotFoundException("Prompt", "id", promptId) }

        val slots = slotRepository.findAllById(request.slotIds)
        if (slots.size != request.slotIds.size) {
            val foundIds = slots.mapNotNull { it.id }
            val missingIds = request.slotIds - foundIds.toSet()
            throw ResourceNotFoundException("Slot", "ids", missingIds.joinToString(", "))
        }

        slots.forEach { slot ->
            prompt.addSlot(slot)
        }

        val savedPrompt = promptRepository.save(prompt)
        return getPromptById(savedPrompt.id!!)
    }

    @Transactional
    fun updatePromptSlots(
        promptId: Long,
        request: UpdatePromptSlotsRequest,
    ): PromptDto {
        val prompt =
            promptRepository
                .findById(promptId)
                .orElseThrow { ResourceNotFoundException("Prompt", "id", promptId) }

        // Clear existing slots
        prompt.clearSlots()

        // Add new slots
        request.slots.forEach { slotRequest ->
            val slot =
                slotRepository
                    .findById(slotRequest.slotId)
                    .orElseThrow { ResourceNotFoundException("Slot", "id", slotRequest.slotId) }
            prompt.addSlot(slot)
        }

        val savedPrompt = promptRepository.save(prompt)
        return getPromptById(savedPrompt.id!!)
    }

    @Transactional
    fun removeSlotFromPrompt(
        promptId: Long,
        slotId: Long,
    ): PromptDto {
        val prompt =
            promptRepository
                .findById(promptId)
                .orElseThrow { ResourceNotFoundException("Prompt", "id", promptId) }

        val slot =
            slotRepository
                .findById(slotId)
                .orElseThrow { ResourceNotFoundException("Slot", "id", slotId) }

        prompt.removeSlot(slot)

        val savedPrompt = promptRepository.save(prompt)
        return getPromptById(savedPrompt.id!!)
    }
}
