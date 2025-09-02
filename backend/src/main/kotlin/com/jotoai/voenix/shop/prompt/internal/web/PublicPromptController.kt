package com.jotoai.voenix.shop.prompt.internal.web

import com.jotoai.voenix.shop.prompt.api.PromptQueryService
import com.jotoai.voenix.shop.prompt.api.dto.prompts.PromptSummaryDto
import com.jotoai.voenix.shop.prompt.api.dto.pub.PublicPromptDto
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/prompts")
class PublicPromptController(
    private val promptQueryService: PromptQueryService,
) {
    @GetMapping
    @Transactional(readOnly = true)
    fun getAllPublicPrompts(): List<PublicPromptDto> = promptQueryService.getAllPublicPrompts()

    @GetMapping("/batch")
    @Transactional(readOnly = true)
    fun getPromptSummariesByIds(
        @RequestParam ids: List<Long>,
    ): List<PromptSummaryDto> = promptQueryService.getPromptSummariesByIds(ids)
}
