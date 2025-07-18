package com.jotoai.voenix.shop.api.public.mugs

import com.jotoai.voenix.shop.domain.mugs.dto.MugSubCategoryDto
import com.jotoai.voenix.shop.domain.mugs.service.MugSubCategoryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/mugs/subcategories")
class PublicMugSubCategoryController(
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
}
