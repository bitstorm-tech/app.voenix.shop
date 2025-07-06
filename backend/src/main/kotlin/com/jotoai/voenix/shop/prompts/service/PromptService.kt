package com.jotoai.voenix.shop.prompts.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.prompts.dto.CreatePromptRequest
import com.jotoai.voenix.shop.prompts.dto.PromptDto
import com.jotoai.voenix.shop.prompts.dto.UpdatePromptRequest
import com.jotoai.voenix.shop.prompts.entity.Prompt
import com.jotoai.voenix.shop.prompts.entity.toDto
import com.jotoai.voenix.shop.prompts.repository.PromptRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptService(
    private val promptRepository: PromptRepository
) {
    
    fun getAllPrompts(): List<PromptDto> = promptRepository.findAll().map { it.toDto() }
    
    fun getPromptById(id: Long): PromptDto {
        return promptRepository.findById(id)
            .map { it.toDto() }
            .orElseThrow { ResourceNotFoundException("Prompt", "id", id) }
    }
    
    fun searchPromptsByTitle(title: String): List<PromptDto> = 
        promptRepository.findByTitleContainingIgnoreCase(title).map { it.toDto() }
    
    @Transactional
    fun createPrompt(request: CreatePromptRequest): PromptDto {
        val prompt = Prompt(
            title = request.title,
            content = request.content
        )
        
        val savedPrompt = promptRepository.save(prompt)
        return savedPrompt.toDto()
    }
    
    @Transactional
    fun updatePrompt(id: Long, request: UpdatePromptRequest): PromptDto {
        val prompt = promptRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Prompt", "id", id) }
        
        request.title?.let { prompt.title = it }
        request.content?.let { prompt.content = it }
        
        val updatedPrompt = promptRepository.save(prompt)
        return updatedPrompt.toDto()
    }
    
    @Transactional
    fun deletePrompt(id: Long) {
        if (!promptRepository.existsById(id)) {
            throw ResourceNotFoundException("Prompt", "id", id)
        }
        promptRepository.deleteById(id)
    }
}