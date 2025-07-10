package com.jotoai.voenix.shop.prompts.controller

import com.jotoai.voenix.shop.prompts.dto.CreatePromptSubCategoryRequest
import com.jotoai.voenix.shop.prompts.dto.PromptSubCategoryDto
import com.jotoai.voenix.shop.prompts.dto.UpdatePromptSubCategoryRequest
import com.jotoai.voenix.shop.prompts.service.PromptSubCategoryService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/prompt-subcategories")
class PromptSubCategoryController(
    private val promptSubCategoryService: PromptSubCategoryService,
) {
    @GetMapping
    fun getAllPromptSubCategories(): ResponseEntity<List<PromptSubCategoryDto>> =
        ResponseEntity.ok(promptSubCategoryService.getAllPromptSubCategories())

    @GetMapping("/{id}")
    fun getPromptSubCategoryById(
        @PathVariable id: Long,
    ): ResponseEntity<PromptSubCategoryDto> = ResponseEntity.ok(promptSubCategoryService.getPromptSubCategoryById(id))

    @GetMapping("/by-category/{categoryId}")
    fun getPromptSubCategoriesByCategory(
        @PathVariable categoryId: Long,
    ): ResponseEntity<List<PromptSubCategoryDto>> = ResponseEntity.ok(promptSubCategoryService.getPromptSubCategoriesByCategory(categoryId))

    @GetMapping("/search")
    fun searchPromptSubCategories(
        @RequestParam name: String,
    ): ResponseEntity<List<PromptSubCategoryDto>> = ResponseEntity.ok(promptSubCategoryService.searchPromptSubCategoriesByName(name))

    @PostMapping
    fun createPromptSubCategory(
        @Valid @RequestBody request: CreatePromptSubCategoryRequest,
    ): ResponseEntity<PromptSubCategoryDto> {
        val createdPromptSubCategory = promptSubCategoryService.createPromptSubCategory(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPromptSubCategory)
    }

    @PutMapping("/{id}")
    fun updatePromptSubCategory(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdatePromptSubCategoryRequest,
    ): ResponseEntity<PromptSubCategoryDto> = ResponseEntity.ok(promptSubCategoryService.updatePromptSubCategory(id, request))

    @DeleteMapping("/{id}")
    fun deletePromptSubCategory(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        promptSubCategoryService.deletePromptSubCategory(id)
        return ResponseEntity.noContent().build()
    }
}
