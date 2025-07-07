package com.jotoai.voenix.shop.prompts.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.prompts.dto.CreatePromptRequest
import com.jotoai.voenix.shop.prompts.dto.PromptDto
import com.jotoai.voenix.shop.prompts.dto.UpdatePromptRequest
import com.jotoai.voenix.shop.prompts.entity.Prompt
import com.jotoai.voenix.shop.prompts.repository.PromptRepository
import com.jotoai.voenix.shop.prompts.repository.PromptCategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptService(
    private val promptRepository: PromptRepository,
    private val promptCategoryRepository: PromptCategoryRepository
) {
    
    fun getAllPrompts(): List<PromptDto> = promptRepository.findAll().map { prompt ->
        if (prompt.categoryId != null) {
            prompt.category = promptCategoryRepository.findById(prompt.categoryId!!).orElse(null)
        }
        prompt.toDto()
    }
    
    fun getPromptById(id: Long): PromptDto {
        return promptRepository.findById(id)
            .map { prompt ->
                if (prompt.categoryId != null) {
                    prompt.category = promptCategoryRepository.findById(prompt.categoryId!!).orElse(null)
                }
                prompt.toDto()
            }
            .orElseThrow { ResourceNotFoundException("Prompt", "id", id) }
    }
    
    fun searchPromptsByTitle(title: String): List<PromptDto> = 
        promptRepository.findByTitleContainingIgnoreCase(title).map { it.toDto() }
    
    @Transactional
    fun createPrompt(request: CreatePromptRequest): PromptDto {
        // Validate category exists if provided
        if (request.categoryId != null && !promptCategoryRepository.existsById(request.categoryId)) {
            throw ResourceNotFoundException("PromptCategory", "id", request.categoryId)
        }
        
        val prompt = Prompt(
            title = request.title,
            content = request.content,
            categoryId = request.categoryId
        )
        
        val savedPrompt = promptRepository.save(prompt)
        
        // Load category for response
        if (savedPrompt.categoryId != null) {
            savedPrompt.category = promptCategoryRepository.findById(savedPrompt.categoryId!!).orElse(null)
        }
        
        return savedPrompt.toDto()
    }
    
    @Transactional
    fun updatePrompt(id: Long, request: UpdatePromptRequest): PromptDto {
        val prompt = promptRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Prompt", "id", id) }
        
        // Validate category exists if provided
        if (request.categoryId != null && !promptCategoryRepository.existsById(request.categoryId)) {
            throw ResourceNotFoundException("PromptCategory", "id", request.categoryId)
        }
        
        request.title?.let { prompt.title = it }
        request.content?.let { prompt.content = it }
        request.categoryId?.let { prompt.categoryId = it }
        request.active?.let { prompt.active = it }
        
        val updatedPrompt = promptRepository.save(prompt)
        
        // Load category for response
        if (updatedPrompt.categoryId != null) {
            updatedPrompt.category = promptCategoryRepository.findById(updatedPrompt.categoryId!!).orElse(null)
        }
        
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