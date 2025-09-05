package com.jotoai.voenix.shop.prompt.internal.web

import com.jotoai.voenix.shop.prompt.PromptSubCategoryDto
import com.jotoai.voenix.shop.prompt.internal.dto.subcategories.CreatePromptSubCategoryRequest
import com.jotoai.voenix.shop.prompt.internal.dto.subcategories.UpdatePromptSubCategoryRequest
import com.jotoai.voenix.shop.prompt.internal.service.PromptSubCategoryServiceImpl
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/prompts/subcategories")
@PreAuthorize("hasRole('ADMIN')")
class AdminPromptSubCategoryController(
    private val promptSubCategoryService: PromptSubCategoryServiceImpl,
) {
    @GetMapping
    fun getAllSubCategories(): List<PromptSubCategoryDto> = promptSubCategoryService.getAllPromptSubCategories()

    @GetMapping("/category/{categoryId}")
    fun getSubCategoriesByCategoryId(
        @PathVariable categoryId: Long,
    ): List<PromptSubCategoryDto> = promptSubCategoryService.getPromptSubCategoriesByCategory(categoryId)

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
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePromptSubCategory(
        @PathVariable id: Long,
    ) {
        promptSubCategoryService.deletePromptSubCategory(id)
    }
}
