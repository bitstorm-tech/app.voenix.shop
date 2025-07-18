package com.jotoai.voenix.shop.api.public.catalog

import com.jotoai.voenix.shop.domain.prompts.dto.PromptCategoryDto
import com.jotoai.voenix.shop.domain.prompts.service.PromptCategoryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/catalog/prompt-categories")
class PublicPromptCategoryController(
    private val promptCategoryService: PromptCategoryService,
) {
    @GetMapping
    fun getAllCategories(): List<PromptCategoryDto> = promptCategoryService.getAllPromptCategories()

    @GetMapping("/{id}")
    fun getCategoryById(
        @PathVariable id: Long,
    ): PromptCategoryDto = promptCategoryService.getPromptCategoryById(id)

    @GetMapping("/search")
    fun searchCategoriesByName(
        @RequestParam name: String,
    ): List<PromptCategoryDto> = promptCategoryService.searchPromptCategoriesByName(name)
}
