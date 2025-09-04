package com.jotoai.voenix.shop.prompt.internal.web

import com.jotoai.voenix.shop.prompt.internal.dto.slotvariants.CreatePromptSlotVariantRequest
import com.jotoai.voenix.shop.prompt.PromptSlotVariantDto
import com.jotoai.voenix.shop.prompt.internal.dto.slotvariants.UpdatePromptSlotVariantRequest
import com.jotoai.voenix.shop.prompt.internal.service.PromptSlotVariantServiceImpl
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
    private val promptSlotVariantService: PromptSlotVariantServiceImpl,
) {
    @GetMapping
    fun getAllSlotVariants(): List<PromptSlotVariantDto> = promptSlotVariantService.getAllSlotVariants()

    @GetMapping("/{id}")
    fun getSlotVariantById(
        @PathVariable id: Long,
    ): PromptSlotVariantDto = promptSlotVariantService.getSlotVariantById(id)

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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteSlotVariant(
        @PathVariable id: Long,
    ) {
        promptSlotVariantService.deleteSlotVariant(id)
    }
}
