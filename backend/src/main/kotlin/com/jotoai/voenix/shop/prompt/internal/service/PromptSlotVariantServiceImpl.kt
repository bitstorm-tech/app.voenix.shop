package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.image.api.ImageStorageService
import com.jotoai.voenix.shop.image.api.dto.ImageType
import com.jotoai.voenix.shop.prompt.api.PromptSlotVariantFacade
import com.jotoai.voenix.shop.prompt.api.PromptSlotVariantQueryService
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.CreatePromptSlotVariantRequest
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.PromptSlotVariantDto
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.UpdatePromptSlotVariantRequest
import com.jotoai.voenix.shop.prompt.api.exceptions.PromptSlotVariantNotFoundException
import com.jotoai.voenix.shop.prompt.internal.entity.PromptSlotVariant
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSlotTypeRepository
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSlotVariantRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException

@Service
@Transactional(readOnly = true)
class PromptSlotVariantServiceImpl(
    private val promptSlotVariantRepository: PromptSlotVariantRepository,
    private val promptSlotTypeRepository: PromptSlotTypeRepository,
    private val promptSlotVariantAssembler: PromptSlotVariantAssembler,
    private val imageStorageService: ImageStorageService,
) : PromptSlotVariantFacade,
    PromptSlotVariantQueryService {

    private val logger = KotlinLogging.logger {}
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
    override fun updateSlotVariant(
        id: Long,
        request: UpdatePromptSlotVariantRequest,
    ): PromptSlotVariantDto {
        // Find existing entity or throw exception
        val promptSlotVariant =
            promptSlotVariantRepository
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

        // Handle image update explicitly
        // The standard ?.let pattern doesn't work here because we need to handle null differently:
        // - null means "remove the image"
        // - field not present in JSON means "don't change the image"
        // Unfortunately, Kotlin data classes can't distinguish between these two cases.
        // So we use a heuristic: if ANY field is provided in the request,
        // then a null exampleImageFilename means delete.
        if (hasAnyFieldProvided(request)) {
            val oldImageFilename = promptSlotVariant.exampleImageFilename
            val newImageFilename = request.exampleImageFilename

            // Delete old image if:
            // 1. We're setting to null (removal)
            // 2. We're changing to a different filename
            if (oldImageFilename != null && oldImageFilename != newImageFilename) {
                try {
                    imageStorageService.deleteFile(oldImageFilename, ImageType.PROMPT_SLOT_VARIANT_EXAMPLE)
                } catch (e: IOException) {
                    logger.warn(e) { "Failed to delete old example image file '$oldImageFilename' during slot variant update. This may result in orphaned files." }
                    // Don't fail the update if image deletion fails
                    // This prevents orphaned files from blocking updates
                }
            }

            // Always update the filename if any field was provided
            // This handles both setting a new filename and clearing it (null)
            promptSlotVariant.exampleImageFilename = newImageFilename
        }

        // Save and return DTO
        val savedEntity = promptSlotVariantRepository.save(promptSlotVariant)
        return promptSlotVariantAssembler.toDto(savedEntity)
    }

    @Transactional
    override fun deleteSlotVariant(id: Long) {
        // Find the entity to get the image filename before deletion
        val promptSlotVariant =
            promptSlotVariantRepository
                .findById(id)
                .orElseThrow { PromptSlotVariantNotFoundException("PromptSlotVariant", "id", id) }

        // Delete the associated image file if it exists
        promptSlotVariant.exampleImageFilename?.let { filename ->
            try {
                imageStorageService.deleteFile(filename, ImageType.PROMPT_SLOT_VARIANT_EXAMPLE)
            } catch (e: IOException) {
                logger.warn(e) { "Failed to delete example image file '$filename' during slot variant deletion. This may result in orphaned files." }
                // Don't fail the deletion if image deletion fails
                // This prevents orphaned files from blocking entity deletion
            }
        }

        // Delete the entity
        promptSlotVariantRepository.deleteById(id)
    }

    private fun hasAnyFieldProvided(request: UpdatePromptSlotVariantRequest): Boolean {
        return request.name != null ||
            request.prompt != null ||
            request.description != null ||
            request.promptSlotTypeId != null ||
            request.exampleImageFilename != null
    }
}
