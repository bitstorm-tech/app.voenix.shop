package com.jotoai.voenix.shop.prompts.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.prompts.dto.CreatePromptCategoryRequest
import com.jotoai.voenix.shop.prompts.dto.PromptCategoryDto
import com.jotoai.voenix.shop.prompts.dto.UpdatePromptCategoryRequest
import com.jotoai.voenix.shop.prompts.entity.PromptCategory
import com.jotoai.voenix.shop.prompts.entity.toDto
import com.jotoai.voenix.shop.prompts.repository.PromptCategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptCategoryService(
    private val promptCategoryRepository: PromptCategoryRepository
) {
    
    fun getAllPromptCategories(): List<PromptCategoryDto> = 
        promptCategoryRepository.findAll().map { it.toDto() }
    
    fun getPromptCategoryById(id: Long): PromptCategoryDto {
        return promptCategoryRepository.findById(id)
            .map { it.toDto() }
            .orElseThrow { ResourceNotFoundException("PromptCategory", "id", id) }
    }
    
    fun searchPromptCategoriesByName(name: String): List<PromptCategoryDto> = 
        promptCategoryRepository.findByNameContainingIgnoreCase(name).map { it.toDto() }
    
    @Transactional
    fun createPromptCategory(request: CreatePromptCategoryRequest): PromptCategoryDto {
        val promptCategory = PromptCategory(
            name = request.name
        )
        
        val savedPromptCategory = promptCategoryRepository.save(promptCategory)
        return savedPromptCategory.toDto()
    }
    
    @Transactional
    fun updatePromptCategory(id: Long, request: UpdatePromptCategoryRequest): PromptCategoryDto {
        val promptCategory = promptCategoryRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("PromptCategory", "id", id) }
        
        request.name?.let { promptCategory.name = it }
        
        val updatedPromptCategory = promptCategoryRepository.save(promptCategory)
        return updatedPromptCategory.toDto()
    }
    
    @Transactional
    fun deletePromptCategory(id: Long) {
        if (!promptCategoryRepository.existsById(id)) {
            throw ResourceNotFoundException("PromptCategory", "id", id)
        }
        promptCategoryRepository.deleteById(id)
    }
}