package com.jotoai.voenix.shop.prompt.internal.web

import com.jotoai.voenix.shop.prompt.internal.service.PromptServiceImpl
import com.jotoai.voenix.shop.prompt.internal.dto.prompts.CreatePromptRequest
import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptDto
import com.jotoai.voenix.shop.prompt.internal.dto.prompts.UpdatePromptRequest
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
@RequestMapping("/api/admin/prompts")
@PreAuthorize("hasRole('ADMIN')")
class AdminPromptController(
    private val promptService: PromptServiceImpl,
) {

    @GetMapping
    fun getAllPrompts(): List<PromptDto> = promptService.getAllPrompts()

    @GetMapping("/{id}")
    fun getPromptById(
        @PathVariable id: Long,
    ): PromptDto = promptService.getPromptById(id)

    @PostMapping
    fun createPrompt(
        @Valid @RequestBody createPromptRequest: CreatePromptRequest,
    ): PromptDto = promptService.createPrompt(createPromptRequest)

    @PutMapping("/{id}")
    fun updatePrompt(
        @PathVariable id: Long,
        @Valid @RequestBody updatePromptRequest: UpdatePromptRequest,
    ): PromptDto = promptService.updatePrompt(id, updatePromptRequest)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePrompt(
        @PathVariable id: Long,
    ) {
        promptService.deletePrompt(id)
    }
}
