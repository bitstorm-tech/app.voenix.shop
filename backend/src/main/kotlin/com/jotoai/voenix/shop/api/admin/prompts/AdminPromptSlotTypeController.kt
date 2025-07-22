package com.jotoai.voenix.shop.api.admin.prompts

import com.jotoai.voenix.shop.domain.prompts.dto.CreatePromptSlotTypeRequest
import com.jotoai.voenix.shop.domain.prompts.dto.PromptSlotTypeDto
import com.jotoai.voenix.shop.domain.prompts.dto.UpdatePromptSlotTypeRequest
import com.jotoai.voenix.shop.domain.prompts.service.PromptSlotTypeService
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
@RequestMapping("/api/admin/prompts/prompt-slot-types")
@PreAuthorize("hasRole('ADMIN')")
class AdminPromptSlotTypeController(
    private val promptSlotTypeService: PromptSlotTypeService,
) {
    @GetMapping
    fun getAllPromptSlotTypes(): List<PromptSlotTypeDto> = promptSlotTypeService.getAllPromptSlotTypes()

    @GetMapping("/{id}")
    fun getPromptSlotTypeById(
        @PathVariable id: Long,
    ): PromptSlotTypeDto = promptSlotTypeService.getPromptSlotTypeById(id)

    @PostMapping
    fun createPromptSlotType(
        @Valid @RequestBody createPromptSlotTypeRequest: CreatePromptSlotTypeRequest,
    ): PromptSlotTypeDto = promptSlotTypeService.createPromptSlotType(createPromptSlotTypeRequest)

    @PutMapping("/{id}")
    fun updatePromptSlotType(
        @PathVariable id: Long,
        @Valid @RequestBody updatePromptSlotTypeRequest: UpdatePromptSlotTypeRequest,
    ): PromptSlotTypeDto = promptSlotTypeService.updatePromptSlotType(id, updatePromptSlotTypeRequest)

    @DeleteMapping("/{id}")
    fun deletePromptSlotType(
        @PathVariable id: Long,
    ) {
        promptSlotTypeService.deletePromptSlotType(id)
    }
}
