package com.jotoai.voenix.shop.prompt.internal.web

import com.jotoai.voenix.shop.prompt.internal.service.PromptCategoryServiceImpl
import com.jotoai.voenix.shop.prompt.internal.dto.categories.CreatePromptCategoryRequest
import com.jotoai.voenix.shop.prompt.internal.dto.categories.PromptCategoryDto
import com.jotoai.voenix.shop.prompt.internal.dto.categories.UpdatePromptCategoryRequest
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
@RequestMapping("/api/admin/prompts/categories")
@PreAuthorize("hasRole('ADMIN')")
class AdminPromptCategoryController(
    private val promptCategoryService: PromptCategoryServiceImpl,
) {
    @GetMapping
    fun getAllCategories(): List<PromptCategoryDto> = promptCategoryService.getAllPromptCategories()

    @PostMapping
    fun createPromptCategory(
        @Valid @RequestBody createPromptCategoryRequest: CreatePromptCategoryRequest,
    ): PromptCategoryDto = promptCategoryService.createPromptCategory(createPromptCategoryRequest)

    @PutMapping("/{id}")
    fun updatePromptCategory(
        @PathVariable id: Long,
        @Valid @RequestBody updatePromptCategoryRequest: UpdatePromptCategoryRequest,
    ): PromptCategoryDto = promptCategoryService.updatePromptCategory(id, updatePromptCategoryRequest)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePromptCategory(
        @PathVariable id: Long,
    ) {
        promptCategoryService.deletePromptCategory(id)
    }
}
