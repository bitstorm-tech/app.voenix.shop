package com.jotoai.voenix.shop.api.public.prompts

import com.jotoai.voenix.shop.domain.prompts.dto.PromptSubCategoryDto
import com.jotoai.voenix.shop.domain.prompts.service.PromptSubCategoryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/prompts/subcategories")
class PublicPromptSubCategoryController(
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
}
