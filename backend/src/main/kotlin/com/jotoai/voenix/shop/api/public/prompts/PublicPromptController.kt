package com.jotoai.voenix.shop.api.public.prompts

import com.jotoai.voenix.shop.domain.prompts.dto.PublicPromptDto
import com.jotoai.voenix.shop.domain.prompts.service.PublicPromptService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/prompts")
class PublicPromptController(
    private val publicPromptService: PublicPromptService,
) {
    @GetMapping
    fun getAllActivePrompts(): List<PublicPromptDto> = publicPromptService.getAllActivePrompts()
}
