package com.jotoai.voenix.shop.api.admin.mugs

import com.jotoai.voenix.shop.domain.mugs.dto.CreateMugSubCategoryRequest
import com.jotoai.voenix.shop.domain.mugs.dto.MugSubCategoryDto
import com.jotoai.voenix.shop.domain.mugs.dto.UpdateMugSubCategoryRequest
import com.jotoai.voenix.shop.domain.mugs.service.MugSubCategoryService
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
@RequestMapping("/api/admin/mugs/subcategories")
@PreAuthorize("hasRole('ADMIN')")
class AdminMugSubCategoryController(
    private val mugSubCategoryService: MugSubCategoryService,
) {
    @GetMapping
    fun getAllSubCategories(): List<MugSubCategoryDto> = mugSubCategoryService.getAllSubCategories()

    @GetMapping("/{id}")
    fun getSubCategoryById(
        @PathVariable id: Long,
    ): MugSubCategoryDto = mugSubCategoryService.getSubCategoryById(id)

    @GetMapping("/category/{categoryId}")
    fun getSubCategoriesByCategoryId(
        @PathVariable categoryId: Long,
    ): List<MugSubCategoryDto> = mugSubCategoryService.getSubCategoriesByCategoryId(categoryId)

    @GetMapping("/search")
    fun searchSubCategoriesByName(
        @RequestParam name: String,
    ): List<MugSubCategoryDto> = mugSubCategoryService.searchSubCategoriesByName(name)

    @PostMapping
    fun createMugSubCategory(
        @Valid @RequestBody createMugSubCategoryRequest: CreateMugSubCategoryRequest,
    ): MugSubCategoryDto = mugSubCategoryService.createSubCategory(createMugSubCategoryRequest)

    @PutMapping("/{id}")
    fun updateMugSubCategory(
        @PathVariable id: Long,
        @Valid @RequestBody updateMugSubCategoryRequest: UpdateMugSubCategoryRequest,
    ): MugSubCategoryDto = mugSubCategoryService.updateSubCategory(id, updateMugSubCategoryRequest)

    @DeleteMapping("/{id}")
    fun deleteMugSubCategory(
        @PathVariable id: Long,
    ) {
        mugSubCategoryService.deleteSubCategory(id)
    }
}
