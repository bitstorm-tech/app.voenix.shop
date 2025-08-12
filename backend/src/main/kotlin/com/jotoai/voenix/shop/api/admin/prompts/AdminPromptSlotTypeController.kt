package com.jotoai.voenix.shop.api.admin.prompts

import com.jotoai.voenix.shop.prompt.api.PromptSlotTypeFacade
import com.jotoai.voenix.shop.prompt.api.PromptSlotTypeQueryService
import com.jotoai.voenix.shop.prompt.api.dto.slottypes.CreatePromptSlotTypeRequest
import com.jotoai.voenix.shop.prompt.api.dto.slottypes.PromptSlotTypeDto
import com.jotoai.voenix.shop.prompt.api.dto.slottypes.UpdatePromptSlotTypeRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
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
    private val promptSlotTypeQueryService: PromptSlotTypeQueryService,
    private val promptSlotTypeFacade: PromptSlotTypeFacade,
) {
    @GetMapping
    fun getAllPromptSlotTypes(): List<PromptSlotTypeDto> = promptSlotTypeQueryService.getAllPromptSlotTypes()

    @GetMapping("/{id}")
    fun getPromptSlotTypeById(
        @PathVariable id: Long,
    ): PromptSlotTypeDto = promptSlotTypeQueryService.getPromptSlotTypeById(id)

    @PostMapping
    fun createPromptSlotType(
        @Valid @RequestBody createPromptSlotTypeRequest: CreatePromptSlotTypeRequest,
    ): PromptSlotTypeDto = promptSlotTypeFacade.createPromptSlotType(createPromptSlotTypeRequest)

    @PutMapping("/{id}")
    fun updatePromptSlotType(
        @PathVariable id: Long,
        @Valid @RequestBody updatePromptSlotTypeRequest: UpdatePromptSlotTypeRequest,
    ): PromptSlotTypeDto = promptSlotTypeFacade.updatePromptSlotType(id, updatePromptSlotTypeRequest)

    @DeleteMapping("/{id}")
    fun deletePromptSlotType(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        promptSlotTypeFacade.deletePromptSlotType(id)
        return ResponseEntity.noContent().build()
    }
}
