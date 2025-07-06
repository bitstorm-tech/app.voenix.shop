package com.jotoai.voenix.shop.prompts.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.prompts.dto.CreatePromptRequest
import com.jotoai.voenix.shop.prompts.dto.PromptDto
import com.jotoai.voenix.shop.prompts.dto.UpdatePromptRequest
import com.jotoai.voenix.shop.prompts.entity.Prompt
import com.jotoai.voenix.shop.prompts.repository.PromptRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptService(
    private val promptRepository: PromptRepository
) {
    
    fun getAllPrompts(): List<PromptDto> {
        return promptRepository.findAll().map { toDto(it) }
    }
    
    fun getPromptById(id: Long): PromptDto {
        return promptRepository.findById(id)
            .map { toDto(it) }
            .orElseThrow { ResourceNotFoundException("Prompt", "id", id) }
    }
    
    fun searchPromptsByTitle(title: String): List<PromptDto> {
        return promptRepository.findByTitleContainingIgnoreCase(title).map { toDto(it) }
    }
    
    @Transactional
    fun createPrompt(request: CreatePromptRequest): PromptDto {
        val prompt = Prompt(
            title = request.title,
            content = request.content
        )
        
        val savedPrompt = promptRepository.save(prompt)
        return toDto(savedPrompt)
    }
    
    @Transactional
    fun updatePrompt(id: Long, request: UpdatePromptRequest): PromptDto {
        val prompt = promptRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Prompt", "id", id) }
        
        request.title?.let { prompt.title = it }
        request.content?.let { prompt.content = it }
        
        val updatedPrompt = promptRepository.save(prompt)
        return toDto(updatedPrompt)
    }
    
    @Transactional
    fun deletePrompt(id: Long) {
        if (!promptRepository.existsById(id)) {
            throw ResourceNotFoundException("Prompt", "id", id)
        }
        promptRepository.deleteById(id)
    }
    
    private fun toDto(prompt: Prompt): PromptDto {
        return PromptDto(
            id = prompt.id!!,
            title = prompt.title,
            content = prompt.content,
            createdAt = prompt.createdAt,
            updatedAt = prompt.updatedAt
        )
    }
}