package com.jotoai.voenix.shop.api.admin.prompts

import com.jotoai.voenix.shop.prompt.api.PromptSlotVariantFacade
import com.jotoai.voenix.shop.prompt.api.PromptSlotVariantQueryService
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.CreatePromptSlotVariantRequest
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.PromptSlotVariantDto
import com.jotoai.voenix.shop.prompt.api.dto.slotvariants.UpdatePromptSlotVariantRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/prompts/slot-variants")
@PreAuthorize("hasRole('ADMIN')")
class AdminPromptSlotVariantController(
    private val promptSlotVariantQueryService: PromptSlotVariantQueryService,
    private val promptSlotVariantFacade: PromptSlotVariantFacade,
) {
    @GetMapping
    fun getAllSlotVariants(): List<PromptSlotVariantDto> = promptSlotVariantQueryService.getAllSlotVariants()

    @GetMapping("/{id}")
    fun getSlotVariantById(
        @PathVariable id: Long,
    ): PromptSlotVariantDto = promptSlotVariantQueryService.getSlotVariantById(id)

    @GetMapping("/type/{typeId}")
    fun getSlotVariantsByTypeId(
        @PathVariable typeId: Long,
    ): List<PromptSlotVariantDto> = promptSlotVariantQueryService.getSlotVariantsBySlotType(typeId)

    @PostMapping
    fun createSlotVariant(
        @Valid @RequestBody createPromptSlotVariantRequest: CreatePromptSlotVariantRequest,
    ): PromptSlotVariantDto = promptSlotVariantFacade.createSlotVariant(createPromptSlotVariantRequest)

    @PutMapping("/{id}")
    fun updateSlotVariant(
        @PathVariable id: Long,
        @Valid @RequestBody updatePromptSlotVariantRequest: UpdatePromptSlotVariantRequest,
    ): PromptSlotVariantDto = promptSlotVariantFacade.updateSlotVariant(id, updatePromptSlotVariantRequest)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteSlotVariant(
        @PathVariable id: Long,
    ) {
        promptSlotVariantFacade.deleteSlotVariant(id)
    }
}
