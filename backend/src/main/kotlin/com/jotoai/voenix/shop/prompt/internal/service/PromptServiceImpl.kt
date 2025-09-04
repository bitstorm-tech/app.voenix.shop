package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.application.ResourceNotFoundException
import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.image.ImageType
import com.jotoai.voenix.shop.prompt.PromptDto
import com.jotoai.voenix.shop.prompt.PromptService
import com.jotoai.voenix.shop.prompt.internal.dto.prompts.CreatePromptRequest
import com.jotoai.voenix.shop.prompt.internal.dto.prompts.PromptSummaryDto
import com.jotoai.voenix.shop.prompt.internal.dto.prompts.UpdatePromptRequest
import com.jotoai.voenix.shop.prompt.internal.dto.pub.PublicPromptDto
import com.jotoai.voenix.shop.prompt.internal.entity.Prompt
import com.jotoai.voenix.shop.prompt.internal.repository.PromptRepository
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSlotVariantRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptServiceImpl(
    private val promptRepository: PromptRepository,
    private val promptSlotVariantRepository: PromptSlotVariantRepository,
    private val imageService: ImageService,
    private val promptAssembler: PromptAssembler,
    private val promptValidator: PromptValidator,
) : PromptService {
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
            }.orElseThrow { ResourceNotFoundException("Prompt", "id", id) }

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
                    promptSlotVariantRepository.getOrNotFound(
                        slotVariantRequest.slotId,
                        "PromptSlotVariant",
                    )
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
        val prompt = promptRepository.getOrNotFound(id, "Prompt")

        // Validate category and subcategory if provided
        promptValidator.validateCategoryExists(request.categoryId)
        promptValidator.validateSubcategoryExists(request.subcategoryId)
        promptValidator.validateSubcategoryBelongsToCategory(request.categoryId, request.subcategoryId)

        request.title?.let { prompt.title = it }
        request.promptText?.let { prompt.promptText = it }
        request.categoryId?.let { prompt.categoryId = it }
        request.subcategoryId?.let { prompt.subcategoryId = it }
        request.active?.let { prompt.active = it }

        // Handle example image filename change when provided
        request.exampleImageFilename?.let { newFilename ->
            val oldFilename = prompt.exampleImageFilename
            if (oldFilename != null && oldFilename != newFilename) {
                ServiceUtils.safeDeleteImage(imageService, oldFilename, ImageType.PROMPT_EXAMPLE, logger)
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
                    promptSlotVariantRepository.getOrNotFound(
                        slotVariantRequest.slotId,
                        "PromptSlotVariant",
                    )
                prompt.addPromptSlotVariant(promptSlotVariant)
            }
        }

        val updatedPrompt = promptRepository.save(prompt)
        val result = getPromptById(updatedPrompt.id!!)

        return result
    }

    @Transactional
    fun deletePrompt(id: Long) {
        val prompt = promptRepository.getOrNotFound(id, "Prompt")

        // Delete associated image if exists
        prompt.exampleImageFilename?.let { filename ->
            ServiceUtils.safeDeleteImage(imageService, filename, ImageType.PROMPT_EXAMPLE, logger)
        }

        promptRepository.deleteById(id)
    }
}
