package com.jotoai.voenix.shop.prompt.internal.web

import com.jotoai.voenix.shop.prompt.internal.service.PromptServiceImpl
import com.jotoai.voenix.shop.prompt.internal.dto.prompts.PromptSummaryDto
import com.jotoai.voenix.shop.prompt.internal.dto.pub.PublicPromptDto
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/prompts")
class PublicPromptController(
    private val promptService: PromptServiceImpl,
) {
    @GetMapping
    @Transactional(readOnly = true)
    fun getAllPublicPrompts(): List<PublicPromptDto> = promptService.getAllPublicPrompts()

    @GetMapping("/batch")
    @Transactional(readOnly = true)
    fun getPromptSummariesByIds(
        @RequestParam ids: List<Long>,
    ): List<PromptSummaryDto> = promptService.getPromptSummariesByIds(ids)
}
