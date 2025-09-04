package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.image.ImageService
import com.jotoai.voenix.shop.image.ImageType
import com.jotoai.voenix.shop.prompt.internal.dto.slotvariants.CreatePromptSlotVariantRequest
import com.jotoai.voenix.shop.prompt.PromptSlotVariantDto
import com.jotoai.voenix.shop.prompt.internal.dto.slotvariants.UpdatePromptSlotVariantRequest
import com.jotoai.voenix.shop.prompt.internal.entity.PromptSlotVariant
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSlotTypeRepository
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSlotVariantRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptSlotVariantServiceImpl(
    private val promptSlotVariantRepository: PromptSlotVariantRepository,
    private val promptSlotTypeRepository: PromptSlotTypeRepository,
    private val promptSlotVariantAssembler: PromptSlotVariantAssembler,
    private val imageService: ImageService,
) {
    private val logger = KotlinLogging.logger {}

    fun getAllSlotVariants(): List<PromptSlotVariantDto> =
        promptSlotVariantRepository.findAll().map { promptSlotVariantAssembler.toDto(it) }

    fun getSlotVariantById(id: Long): PromptSlotVariantDto =
        promptSlotVariantAssembler.toDto(
            promptSlotVariantRepository.getOrNotFound(id, "PromptSlotVariant"),
        )

    fun existsById(id: Long): Boolean = promptSlotVariantRepository.existsById(id)

    @Transactional
    fun createSlotVariant(request: CreatePromptSlotVariantRequest): PromptSlotVariantDto {
        // Validate that promptSlotTypeId exists
        require(promptSlotTypeRepository.existsById(request.promptSlotTypeId)) {
            "PromptSlotType with id '${request.promptSlotTypeId}' does not exist"
        }

        // Check name uniqueness
        require(!promptSlotVariantRepository.existsByName(request.name)) {
            "PromptSlotVariant with name '${request.name}' already exists"
        }

        // Create new PromptSlotVariant entity
        val promptSlotVariant =
            PromptSlotVariant(
                promptSlotTypeId = request.promptSlotTypeId,
                name = request.name,
                prompt = request.prompt,
                description = request.description,
                exampleImageFilename = request.exampleImageFilename,
            )

        // Save to repository and return DTO
        val savedEntity = promptSlotVariantRepository.save(promptSlotVariant)
        return promptSlotVariantAssembler.toDto(savedEntity)
    }

    @Transactional
    fun updateSlotVariant(
        id: Long,
        request: UpdatePromptSlotVariantRequest,
    ): PromptSlotVariantDto {
        // Find existing entity or throw exception
        val promptSlotVariant = promptSlotVariantRepository.getOrNotFound(id, "PromptSlotVariant")

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

        // Handle image update explicitly using heuristic to distinguish intent
        if (hasAnyFieldProvided(request)) {
            val oldImageFilename = promptSlotVariant.exampleImageFilename
            val newImageFilename = request.exampleImageFilename
            updateExampleImage(promptSlotVariant, oldImageFilename, newImageFilename)
        }

        // Save and return DTO
        val savedEntity = promptSlotVariantRepository.save(promptSlotVariant)
        return promptSlotVariantAssembler.toDto(savedEntity)
    }

    @Transactional
    fun deleteSlotVariant(id: Long) {
        // Find the entity to get the image filename before deletion
        val promptSlotVariant = promptSlotVariantRepository.getOrNotFound(id, "PromptSlotVariant")

        // Delete the associated image file if it exists
        promptSlotVariant.exampleImageFilename?.let { filename ->
            ServiceUtils.safeDeleteImage(imageService, filename, ImageType.PROMPT_SLOT_VARIANT_EXAMPLE, logger)
        }

        // Delete the entity
        promptSlotVariantRepository.deleteById(id)
    }

    private fun hasAnyFieldProvided(request: UpdatePromptSlotVariantRequest): Boolean =
        request.name != null ||
            request.prompt != null ||
            request.description != null ||
            request.promptSlotTypeId != null ||
            request.exampleImageFilename != null

    private fun updateExampleImage(
        entity: PromptSlotVariant,
        oldFilename: String?,
        newFilename: String?,
    ) {
        if (oldFilename != null && oldFilename != newFilename) {
            ServiceUtils.safeDeleteImage(imageService, oldFilename, ImageType.PROMPT_SLOT_VARIANT_EXAMPLE, logger)
        }
        entity.exampleImageFilename = newFilename
    }
}
