package com.jotoai.voenix.shop.api.public.prompts

import com.jotoai.voenix.shop.domain.prompts.dto.PromptDto
import com.jotoai.voenix.shop.domain.prompts.service.PromptService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/prompts")
class PublicPromptController(
    private val promptService: PromptService,
) {
    @GetMapping
    fun getAllPrompts(): List<PromptDto> = promptService.getAllPrompts()

    @GetMapping("/{id}")
    fun getPromptById(
        @PathVariable id: Long,
    ): PromptDto = promptService.getPromptById(id)

    @GetMapping("/search")
    fun searchPromptsByTitle(
        @RequestParam title: String,
    ): List<PromptDto> = promptService.searchPromptsByTitle(title)
}
