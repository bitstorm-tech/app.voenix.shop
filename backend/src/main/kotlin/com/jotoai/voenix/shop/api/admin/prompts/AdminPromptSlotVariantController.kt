package com.jotoai.voenix.shop.api.admin.prompts

import com.jotoai.voenix.shop.domain.prompts.dto.CreatePromptSlotVariantRequest
import com.jotoai.voenix.shop.domain.prompts.dto.PromptSlotVariantDto
import com.jotoai.voenix.shop.domain.prompts.dto.UpdatePromptSlotVariantRequest
import com.jotoai.voenix.shop.domain.prompts.service.PromptSlotVariantService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/prompts/slot-variants")
@PreAuthorize("hasRole('ADMIN')")
class AdminPromptSlotVariantController(
    private val promptSlotVariantService: PromptSlotVariantService,
) {
    @GetMapping
    fun getAllSlotVariants(): List<PromptSlotVariantDto> = promptSlotVariantService.getAllSlotVariants()

    @GetMapping("/{id}")
    fun getSlotVariantById(
        @PathVariable id: Long,
    ): PromptSlotVariantDto = promptSlotVariantService.getSlotVariantById(id)

    @GetMapping("/type/{typeId}")
    fun getSlotVariantsByTypeId(
        @PathVariable typeId: Long,
    ): List<PromptSlotVariantDto> = promptSlotVariantService.getSlotVariantsBySlotType(typeId)

    @PostMapping
    fun createSlotVariant(
        @Valid @RequestBody createPromptSlotVariantRequest: CreatePromptSlotVariantRequest,
    ): PromptSlotVariantDto = promptSlotVariantService.createSlotVariant(createPromptSlotVariantRequest)

    @PutMapping("/{id}")
    fun updateSlotVariant(
        @PathVariable id: Long,
        @Valid @RequestBody updatePromptSlotVariantRequest: UpdatePromptSlotVariantRequest,
    ): PromptSlotVariantDto = promptSlotVariantService.updateSlotVariant(id, updatePromptSlotVariantRequest)

    @DeleteMapping("/{id}")
    fun deleteSlotVariant(
        @PathVariable id: Long,
    ) {
        promptSlotVariantService.deleteSlotVariant(id)
    }
}
