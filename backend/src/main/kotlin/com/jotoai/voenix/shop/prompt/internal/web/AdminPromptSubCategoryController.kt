package com.jotoai.voenix.shop.prompt.internal.web

import com.jotoai.voenix.shop.prompt.api.PromptSubCategoryFacade
import com.jotoai.voenix.shop.prompt.api.PromptSubCategoryQueryService
import com.jotoai.voenix.shop.prompt.api.dto.subcategories.CreatePromptSubCategoryRequest
import com.jotoai.voenix.shop.prompt.api.dto.subcategories.PromptSubCategoryDto
import com.jotoai.voenix.shop.prompt.api.dto.subcategories.UpdatePromptSubCategoryRequest
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
    private val promptSubCategoryQueryService: PromptSubCategoryQueryService,
    private val promptSubCategoryFacade: PromptSubCategoryFacade,
) {
    @GetMapping
    fun getAllSubCategories(): List<PromptSubCategoryDto> = promptSubCategoryQueryService.getAllPromptSubCategories()

    @GetMapping("/category/{categoryId}")
    fun getSubCategoriesByCategoryId(
        @PathVariable categoryId: Long,
    ): List<PromptSubCategoryDto> = promptSubCategoryQueryService.getPromptSubCategoriesByCategory(categoryId)

    @PostMapping
    fun createPromptSubCategory(
        @Valid @RequestBody createPromptSubCategoryRequest: CreatePromptSubCategoryRequest,
    ): PromptSubCategoryDto = promptSubCategoryFacade.createPromptSubCategory(createPromptSubCategoryRequest)

    @PutMapping("/{id}")
    fun updatePromptSubCategory(
        @PathVariable id: Long,
        @Valid @RequestBody updatePromptSubCategoryRequest: UpdatePromptSubCategoryRequest,
    ): PromptSubCategoryDto = promptSubCategoryFacade.updatePromptSubCategory(id, updatePromptSubCategoryRequest)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePromptSubCategory(
        @PathVariable id: Long,
    ) {
        promptSubCategoryFacade.deletePromptSubCategory(id)
    }
}
