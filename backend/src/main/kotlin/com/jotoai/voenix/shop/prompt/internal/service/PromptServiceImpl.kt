package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.image.ImageType
import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.prompt.api.dto.prompts.CreatePromptRequest
import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto
import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptSummaryDto
import com.jotoai.voenix.shop.prompt.api.dto.prompts.UpdatePromptRequest
import com.jotoai.voenix.shop.prompt.api.dto.pub.PublicPromptDto
import com.jotoai.voenix.shop.prompt.api.exceptions.PromptNotFoundException
import com.jotoai.voenix.shop.prompt.internal.entity.Prompt
import com.jotoai.voenix.shop.prompt.internal.repository.PromptRepository
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSlotVariantRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException

@Service
@Transactional(readOnly = true)
class PromptServiceImpl(
    private val promptRepository: PromptRepository,
    private val promptSlotVariantRepository: PromptSlotVariantRepository,
    private val imageService: ImageService,
    private val promptAssembler: PromptAssembler,
    private val promptValidator: PromptValidator,
) : PromptQueryService {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun getAllPrompts(): List<PromptDto> =
        promptRepository.findAllWithRelations().map { prompt ->
            promptAssembler.toDto(prompt)
        }

    override fun getPromptById(id: Long): PromptDto =
        promptRepository
            .findByIdWithRelations(id)
            .map { prompt ->
                promptAssembler.toDto(prompt)
            }.orElseThrow { PromptNotFoundException("Prompt", "id", id) }

    override fun existsById(id: Long): Boolean = promptRepository.existsById(id)

    fun getAllPublicPrompts(): List<PublicPromptDto> =
        promptRepository.findAllActiveWithRelations().map { prompt ->
            promptAssembler.toPublicDto(prompt)
        }

    fun getPromptSummariesByIds(ids: List<Long>): List<PromptSummaryDto> {
        if (ids.isEmpty()) return emptyList()

        return promptRepository
            .findAllById(ids)
            .map { prompt ->
                PromptSummaryDto(
                    id = requireNotNull(prompt.id) { "Prompt ID cannot be null" },
                    title = prompt.title,
                )
            }
    }

    @Transactional
    fun createPrompt(request: CreatePromptRequest): PromptDto {
        // Validate the entire prompt request
        val slotVariantIds = request.slots.map { it.slotId }
        promptValidator.validatePromptRequest(request.categoryId, request.subcategoryId, slotVariantIds)

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
                        .orElseThrow { PromptNotFoundException("Slot variant", "id", slotVariantRequest.slotId) }
                prompt.addPromptSlotVariant(slotVariant)
            }
        }

        val savedPrompt = promptRepository.save(prompt)
        val result = getPromptById(savedPrompt.id!!)

        return result
    }

    @Transactional
    fun updatePrompt(
        id: Long,
        request: UpdatePromptRequest,
    ): PromptDto {
        val prompt =
            promptRepository
                .findById(id)
                .orElseThrow { PromptNotFoundException("Prompt", "id", id) }

        // Validate category and subcategory if provided
        promptValidator.validateCategoryExists(request.categoryId)
        promptValidator.validateSubcategoryExists(request.subcategoryId)
        promptValidator.validateSubcategoryBelongsToCategory(request.categoryId, request.subcategoryId)

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
                } catch (e: IOException) {
                    logger.warn(e) { "Failed to delete old prompt example image: $oldFilename" }
                }
            }
            prompt.exampleImageFilename = newFilename
        }

        // Update slot variants if provided
        request.slots?.let { slotVariants ->
            val slotVariantIds = slotVariants.map { it.slotId }
            promptValidator.validateSlotVariantsExist(slotVariantIds)

            prompt.clearPromptSlotVariants()
            slotVariants.forEach { slotVariantRequest ->
                val promptSlotVariant =
                    promptSlotVariantRepository
                        .findById(slotVariantRequest.slotId)
                        .orElseThrow { PromptNotFoundException("Prompt slot variant", "id", slotVariantRequest.slotId) }
                prompt.addPromptSlotVariant(promptSlotVariant)
            }
        }

        val updatedPrompt = promptRepository.save(prompt)
        val result = getPromptById(updatedPrompt.id!!)

        return result
    }

    @Transactional
    fun deletePrompt(id: Long) {
        val prompt =
            promptRepository
                .findById(id)
                .orElseThrow { PromptNotFoundException("Prompt", "id", id) }

        // Delete associated image if exists
        prompt.exampleImageFilename?.let { filename ->
            try {
                imageService.delete(filename, ImageType.PROMPT_EXAMPLE)
            } catch (e: IOException) {
                logger.warn(e) { "Failed to delete prompt example image during prompt deletion: $filename" }
            }
        }

        promptRepository.deleteById(id)
    }
}
