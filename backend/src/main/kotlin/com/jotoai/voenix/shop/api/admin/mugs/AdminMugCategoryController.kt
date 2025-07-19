package com.jotoai.voenix.shop.api.admin.mugs

import com.jotoai.voenix.shop.domain.mugs.dto.CreateMugCategoryRequest
import com.jotoai.voenix.shop.domain.mugs.dto.MugCategoryDto
import com.jotoai.voenix.shop.domain.mugs.dto.UpdateMugCategoryRequest
import com.jotoai.voenix.shop.domain.mugs.service.MugCategoryService
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
@RequestMapping("/api/admin/mugs/categories")
@PreAuthorize("hasRole('ADMIN')")
class AdminMugCategoryController(
    private val mugCategoryService: MugCategoryService,
) {
    @GetMapping
    fun getAllCategories(): List<MugCategoryDto> = mugCategoryService.getAllCategories()

    @GetMapping("/{id}")
    fun getCategoryById(
        @PathVariable id: Long,
    ): MugCategoryDto = mugCategoryService.getCategoryById(id)

    @GetMapping("/search")
    fun searchCategoriesByName(
        @RequestParam name: String,
    ): List<MugCategoryDto> = mugCategoryService.searchCategoriesByName(name)

    @PostMapping
    fun createMugCategory(
        @Valid @RequestBody createMugCategoryRequest: CreateMugCategoryRequest,
    ): MugCategoryDto = mugCategoryService.createCategory(createMugCategoryRequest)

    @PutMapping("/{id}")
    fun updateMugCategory(
        @PathVariable id: Long,
        @Valid @RequestBody updateMugCategoryRequest: UpdateMugCategoryRequest,
    ): MugCategoryDto = mugCategoryService.updateCategory(id, updateMugCategoryRequest)

    @DeleteMapping("/{id}")
    fun deleteMugCategory(
        @PathVariable id: Long,
    ) {
        mugCategoryService.deleteCategory(id)
    }
}
