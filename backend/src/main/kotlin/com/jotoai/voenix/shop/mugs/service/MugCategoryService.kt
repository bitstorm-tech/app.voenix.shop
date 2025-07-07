package com.jotoai.voenix.shop.mugs.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.mugs.dto.CreateMugCategoryRequest
import com.jotoai.voenix.shop.mugs.dto.MugCategoryDto
import com.jotoai.voenix.shop.mugs.dto.UpdateMugCategoryRequest
import com.jotoai.voenix.shop.mugs.entity.MugCategory
import com.jotoai.voenix.shop.mugs.repository.MugCategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MugCategoryService(
    private val mugCategoryRepository: MugCategoryRepository
) {
    
    fun getAllCategories(): List<MugCategoryDto> = 
        mugCategoryRepository.findAll().map { it.toDto() }
    
    fun getCategoryById(id: Long): MugCategoryDto {
        return mugCategoryRepository.findById(id)
            .map { it.toDto() }
            .orElseThrow { ResourceNotFoundException("MugCategory", "id", id) }
    }
    
    fun searchCategoriesByName(name: String): List<MugCategoryDto> = 
        mugCategoryRepository.findByNameContainingIgnoreCase(name).map { it.toDto() }
    
    @Transactional
    fun createCategory(request: CreateMugCategoryRequest): MugCategoryDto {
        if (mugCategoryRepository.existsByNameIgnoreCase(request.name)) {
            throw IllegalArgumentException("Category with name '${request.name}' already exists")
        }
        
        val category = MugCategory(
            name = request.name,
            description = request.description
        )
        
        val savedCategory = mugCategoryRepository.save(category)
        return savedCategory.toDto()
    }
    
    @Transactional
    fun updateCategory(id: Long, request: UpdateMugCategoryRequest): MugCategoryDto {
        val category = mugCategoryRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("MugCategory", "id", id) }
        
        request.name?.let { newName ->
            if (newName != category.name && mugCategoryRepository.existsByNameIgnoreCase(newName)) {
                throw IllegalArgumentException("Category with name '$newName' already exists")
            }
            category.name = newName
        }
        
        request.description?.let { category.description = it }
        
        val updatedCategory = mugCategoryRepository.save(category)
        return updatedCategory.toDto()
    }
    
    @Transactional
    fun deleteCategory(id: Long) {
        if (!mugCategoryRepository.existsById(id)) {
            throw ResourceNotFoundException("MugCategory", "id", id)
        }
        mugCategoryRepository.deleteById(id)
    }
}