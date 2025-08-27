package com.jotoai.voenix.shop.prompt.web

import com.jotoai.voenix.shop.prompt.api.dto.pub.PublicPromptDto
import com.jotoai.voenix.shop.prompt.internal.repository.PromptRepository
import com.jotoai.voenix.shop.prompt.internal.service.PromptAssembler
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/prompts")
class PublicPromptController(
    private val promptRepository: PromptRepository,
    private val promptAssembler: PromptAssembler,
) {
    @GetMapping
    @Transactional(readOnly = true)
    fun getAllPublicPrompts(): List<PublicPromptDto> =
        promptRepository
            .findAllActiveWithRelations()
            .map { promptAssembler.toPublicDto(it) }
}
