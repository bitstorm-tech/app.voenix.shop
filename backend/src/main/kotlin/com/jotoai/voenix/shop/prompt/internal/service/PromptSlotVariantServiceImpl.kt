package com.jotoai.voenix.shop.prompt.internal.service

import com.jotoai.voenix.shop.image.api.ImageStorageService
import com.jotoai.voenix.shop.prompt.api.PromptSlotVariantFacade
import com.jotoai.voenix.shop.prompt.api.PromptSlotVariantQueryService
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.CreatePromptSlotVariantRequest
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.PromptSlotVariantDto
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.UpdatePromptSlotVariantRequest
import com.jotoai.voenix.shop.prompt.api.exceptions.PromptSlotVariantNotFoundException
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSlotTypeRepository
import com.jotoai.voenix.shop.prompt.internal.repository.PromptSlotVariantRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptSlotVariantServiceImpl(
    private val promptSlotVariantRepository: PromptSlotVariantRepository,
    private val promptSlotTypeRepository: PromptSlotTypeRepository,
    private val imageStorageService: ImageStorageService,
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
        // Basic implementation - in reality would need the full entity creation logic
        throw NotImplementedError("createSlotVariant not fully implemented - would need entity creation logic")
    }

    @Transactional
    override fun updateSlotVariant(
        id: Long,
        request: UpdatePromptSlotVariantRequest,
    ): PromptSlotVariantDto {
        // Basic implementation - in reality would need the full entity update logic
        throw NotImplementedError("updateSlotVariant not fully implemented - would need entity update logic")
    }

    @Transactional
    override fun deleteSlotVariant(id: Long) {
        // Basic implementation - in reality would need the full entity deletion logic
        throw NotImplementedError("deleteSlotVariant not fully implemented - would need entity deletion logic")
    }
}
