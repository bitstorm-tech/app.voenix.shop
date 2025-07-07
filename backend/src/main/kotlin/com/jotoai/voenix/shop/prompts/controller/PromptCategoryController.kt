package com.jotoai.voenix.shop.prompts.controller

import com.jotoai.voenix.shop.prompts.dto.CreatePromptCategoryRequest
import com.jotoai.voenix.shop.prompts.dto.PromptCategoryDto
import com.jotoai.voenix.shop.prompts.dto.UpdatePromptCategoryRequest
import com.jotoai.voenix.shop.prompts.service.PromptCategoryService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/prompt-categories")
class PromptCategoryController(
    private val promptCategoryService: PromptCategoryService
) {
    
    @GetMapping
    fun getAllPromptCategories(): ResponseEntity<List<PromptCategoryDto>> = 
        ResponseEntity.ok(promptCategoryService.getAllPromptCategories())
    
    @GetMapping("/{id}")
    fun getPromptCategoryById(@PathVariable id: Long): ResponseEntity<PromptCategoryDto> = 
        ResponseEntity.ok(promptCategoryService.getPromptCategoryById(id))
    
    @GetMapping("/search")
    fun searchPromptCategories(@RequestParam name: String): ResponseEntity<List<PromptCategoryDto>> = 
        ResponseEntity.ok(promptCategoryService.searchPromptCategoriesByName(name))
    
    @PostMapping
    fun createPromptCategory(@Valid @RequestBody request: CreatePromptCategoryRequest): ResponseEntity<PromptCategoryDto> {
        val createdPromptCategory = promptCategoryService.createPromptCategory(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPromptCategory)
    }
    
    @PutMapping("/{id}")
    fun updatePromptCategory(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdatePromptCategoryRequest
    ): ResponseEntity<PromptCategoryDto> = 
        ResponseEntity.ok(promptCategoryService.updatePromptCategory(id, request))
    
    @DeleteMapping("/{id}")
    fun deletePromptCategory(@PathVariable id: Long): ResponseEntity<Void> {
        promptCategoryService.deletePromptCategory(id)
        return ResponseEntity.noContent().build()
    }
}