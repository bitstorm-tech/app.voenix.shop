package com.jotoai.voenix.shop.api.admin.prompts

import com.jotoai.voenix.shop.domain.prompts.dto.AddSlotsRequest
import com.jotoai.voenix.shop.domain.prompts.dto.CreatePromptRequest
import com.jotoai.voenix.shop.domain.prompts.dto.PromptDto
import com.jotoai.voenix.shop.domain.prompts.dto.UpdatePromptRequest
import com.jotoai.voenix.shop.domain.prompts.dto.UpdatePromptSlotsRequest
import com.jotoai.voenix.shop.domain.prompts.service.PromptService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/prompts")
@PreAuthorize("hasRole('ADMIN')")
class AdminPromptController(
    private val promptService: PromptService,
) {
    @PostMapping
    fun createPrompt(
        @Valid @RequestBody createPromptRequest: CreatePromptRequest,
    ): PromptDto = promptService.createPrompt(createPromptRequest)

    @PutMapping("/{id}")
    fun updatePrompt(
        @PathVariable id: Long,
        @Valid @RequestBody updatePromptRequest: UpdatePromptRequest,
    ): PromptDto = promptService.updatePrompt(id, updatePromptRequest)

    @PutMapping("/{id}/slots")
    fun updatePromptSlots(
        @PathVariable id: Long,
        @Valid @RequestBody updatePromptSlotsRequest: UpdatePromptSlotsRequest,
    ): PromptDto = promptService.updatePromptSlots(id, updatePromptSlotsRequest)

    @PostMapping("/{id}/slots")
    fun addSlotsToPrompt(
        @PathVariable id: Long,
        @Valid @RequestBody addSlotsRequest: AddSlotsRequest,
    ): PromptDto = promptService.addSlotsToPrompt(id, addSlotsRequest)

    @DeleteMapping("/{id}")
    fun deletePrompt(
        @PathVariable id: Long,
    ) {
        promptService.deletePrompt(id)
    }
}
