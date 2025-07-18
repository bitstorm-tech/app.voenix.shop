package com.jotoai.voenix.shop.api.admin.catalog

import com.jotoai.voenix.shop.domain.prompts.dto.CreatePromptCategoryRequest
import com.jotoai.voenix.shop.domain.prompts.dto.PromptCategoryDto
import com.jotoai.voenix.shop.domain.prompts.dto.UpdatePromptCategoryRequest
import com.jotoai.voenix.shop.domain.prompts.service.PromptCategoryService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/catalog/prompt-categories")
@PreAuthorize("hasRole('ADMIN')")
class AdminPromptCategoryController(
    private val promptCategoryService: PromptCategoryService,
) {
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
    fun deletePromptCategory(
        @PathVariable id: Long,
    ) {
        promptCategoryService.deletePromptCategory(id)
    }
}
