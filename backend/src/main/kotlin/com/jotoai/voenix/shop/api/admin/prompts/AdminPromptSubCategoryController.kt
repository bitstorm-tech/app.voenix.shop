package com.jotoai.voenix.shop.api.admin.prompts

import com.jotoai.voenix.shop.domain.prompts.dto.CreatePromptSubCategoryRequest
import com.jotoai.voenix.shop.domain.prompts.dto.PromptSubCategoryDto
import com.jotoai.voenix.shop.domain.prompts.dto.UpdatePromptSubCategoryRequest
import com.jotoai.voenix.shop.domain.prompts.service.PromptSubCategoryService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
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
@RequestMapping("/api/admin/prompts/subcategories")
@PreAuthorize("hasRole('ADMIN')")
class AdminPromptSubCategoryController(
    private val promptSubCategoryService: PromptSubCategoryService,
) {
    @GetMapping
    fun getAllSubCategories(): List<PromptSubCategoryDto> = promptSubCategoryService.getAllPromptSubCategories()

    @GetMapping("/{id}")
    fun getSubCategoryById(
        @PathVariable id: Long,
    ): PromptSubCategoryDto = promptSubCategoryService.getPromptSubCategoryById(id)

    @GetMapping("/category/{categoryId}")
    fun getSubCategoriesByCategoryId(
        @PathVariable categoryId: Long,
    ): List<PromptSubCategoryDto> = promptSubCategoryService.getPromptSubCategoriesByCategory(categoryId)

    @GetMapping("/search")
    fun searchSubCategoriesByName(
        @RequestParam name: String,
    ): List<PromptSubCategoryDto> = promptSubCategoryService.searchPromptSubCategoriesByName(name)

    @PostMapping
    fun createPromptSubCategory(
        @Valid @RequestBody createPromptSubCategoryRequest: CreatePromptSubCategoryRequest,
    ): PromptSubCategoryDto = promptSubCategoryService.createPromptSubCategory(createPromptSubCategoryRequest)

    @PutMapping("/{id}")
    fun updatePromptSubCategory(
        @PathVariable id: Long,
        @Valid @RequestBody updatePromptSubCategoryRequest: UpdatePromptSubCategoryRequest,
    ): PromptSubCategoryDto = promptSubCategoryService.updatePromptSubCategory(id, updatePromptSubCategoryRequest)

    @DeleteMapping("/{id}")
    fun deletePromptSubCategory(
        @PathVariable id: Long,
    ) {
        promptSubCategoryService.deletePromptSubCategory(id)
    }
}
