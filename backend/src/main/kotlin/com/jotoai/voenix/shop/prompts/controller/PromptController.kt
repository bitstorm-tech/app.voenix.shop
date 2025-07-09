package com.jotoai.voenix.shop.prompts.controller

import com.jotoai.voenix.shop.prompts.dto.AddSlotsRequest
import com.jotoai.voenix.shop.prompts.dto.CreatePromptRequest
import com.jotoai.voenix.shop.prompts.dto.PromptDto
import com.jotoai.voenix.shop.prompts.dto.UpdatePromptRequest
import com.jotoai.voenix.shop.prompts.dto.UpdatePromptSlotsRequest
import com.jotoai.voenix.shop.prompts.service.PromptService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/prompts")
class PromptController(
    private val promptService: PromptService,
) {
    @GetMapping
    fun getAllPrompts(): ResponseEntity<List<PromptDto>> = ResponseEntity.ok(promptService.getAllPrompts())

    @GetMapping("/{id}")
    fun getPromptById(
        @PathVariable id: Long,
    ): ResponseEntity<PromptDto> = ResponseEntity.ok(promptService.getPromptById(id))

    @GetMapping("/search")
    fun searchPrompts(
        @RequestParam title: String,
    ): ResponseEntity<List<PromptDto>> = ResponseEntity.ok(promptService.searchPromptsByTitle(title))

    @PostMapping
    fun createPrompt(
        @Valid @RequestBody request: CreatePromptRequest,
    ): ResponseEntity<PromptDto> {
        val createdPrompt = promptService.createPrompt(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPrompt)
    }

    @PutMapping("/{id}")
    fun updatePrompt(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdatePromptRequest,
    ): ResponseEntity<PromptDto> = ResponseEntity.ok(promptService.updatePrompt(id, request))

    @DeleteMapping("/{id}")
    fun deletePrompt(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        promptService.deletePrompt(id)
        return ResponseEntity.noContent().build()
    }

    // Slot management endpoints
    @PostMapping("/{id}/slots")
    fun addSlotsToPrompt(
        @PathVariable id: Long,
        @Valid @RequestBody request: AddSlotsRequest,
    ): ResponseEntity<PromptDto> = ResponseEntity.ok(promptService.addSlotsToPrompt(id, request))

    @PutMapping("/{id}/slots")
    fun updatePromptSlots(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdatePromptSlotsRequest,
    ): ResponseEntity<PromptDto> = ResponseEntity.ok(promptService.updatePromptSlots(id, request))

    @DeleteMapping("/{id}/slots/{slotId}")
    fun removeSlotFromPrompt(
        @PathVariable id: Long,
        @PathVariable slotId: Long,
    ): ResponseEntity<PromptDto> = ResponseEntity.ok(promptService.removeSlotFromPrompt(id, slotId))
}
