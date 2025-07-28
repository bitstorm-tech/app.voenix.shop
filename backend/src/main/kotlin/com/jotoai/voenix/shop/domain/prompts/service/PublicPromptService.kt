package com.jotoai.voenix.shop.domain.prompts.service

import com.jotoai.voenix.shop.domain.prompts.dto.PublicPromptDto
import com.jotoai.voenix.shop.domain.prompts.repository.PromptRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PublicPromptService(
    private val promptRepository: PromptRepository,
) {
    @Cacheable(value = ["publicPrompts"], key = "'all'")
    fun getAllActivePrompts(): List<PublicPromptDto> =
        promptRepository.findAllActiveWithRelations().map { prompt ->
            prompt.toPublicDto()
        }
}
