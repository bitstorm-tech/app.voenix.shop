package com.jotoai.voenix.shop.api.public.mugs

import com.jotoai.voenix.shop.domain.mugs.dto.MugCategoryDto
import com.jotoai.voenix.shop.domain.mugs.service.MugCategoryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/public/mugs/categories")
class PublicMugCategoryController(
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
}
