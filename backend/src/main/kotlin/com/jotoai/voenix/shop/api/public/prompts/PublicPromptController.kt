package com.jotoai.voenix.shop.api.public.prompts

import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.prompt.api.dto.pub.PublicPromptDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/prompts")
class PublicPromptController(
    private val promptQueryService: PromptQueryService,
) {
    @GetMapping
    fun getAllActivePrompts(): List<PublicPromptDto> = promptQueryService.getAllActivePrompts()
}
