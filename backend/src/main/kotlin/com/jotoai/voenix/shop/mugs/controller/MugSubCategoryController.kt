package com.jotoai.voenix.shop.mugs.controller

import com.jotoai.voenix.shop.mugs.dto.CreateMugSubCategoryRequest
import com.jotoai.voenix.shop.mugs.dto.MugSubCategoryDto
import com.jotoai.voenix.shop.mugs.dto.UpdateMugSubCategoryRequest
import com.jotoai.voenix.shop.mugs.service.MugSubCategoryService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
@RequestMapping("/api/mug-sub-categories")
class MugSubCategoryController(
    private val mugSubCategoryService: MugSubCategoryService,
) {
    @GetMapping
    fun getAllSubCategories(): ResponseEntity<List<MugSubCategoryDto>> = ResponseEntity.ok(mugSubCategoryService.getAllSubCategories())

    @GetMapping("/{id}")
    fun getSubCategoryById(
        @PathVariable id: Long,
    ): ResponseEntity<MugSubCategoryDto> = ResponseEntity.ok(mugSubCategoryService.getSubCategoryById(id))

    @GetMapping("/category/{categoryId}")
    fun getSubCategoriesByCategoryId(
        @PathVariable categoryId: Long,
    ): ResponseEntity<List<MugSubCategoryDto>> = ResponseEntity.ok(mugSubCategoryService.getSubCategoriesByCategoryId(categoryId))

    @GetMapping("/search")
    fun searchSubCategories(
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam name: String,
    ): ResponseEntity<List<MugSubCategoryDto>> {
        val results =
            if (categoryId != null) {
                mugSubCategoryService.searchSubCategoriesByCategoryAndName(categoryId, name)
            } else {
                mugSubCategoryService.searchSubCategoriesByName(name)
            }
        return ResponseEntity.ok(results)
    }

    @PostMapping
    fun createSubCategory(
        @Valid @RequestBody request: CreateMugSubCategoryRequest,
    ): ResponseEntity<MugSubCategoryDto> {
        val createdSubCategory = mugSubCategoryService.createSubCategory(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubCategory)
    }

    @PutMapping("/{id}")
    fun updateSubCategory(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateMugSubCategoryRequest,
    ): ResponseEntity<MugSubCategoryDto> = ResponseEntity.ok(mugSubCategoryService.updateSubCategory(id, request))

    @DeleteMapping("/{id}")
    fun deleteSubCategory(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        mugSubCategoryService.deleteSubCategory(id)
        return ResponseEntity.noContent().build()
    }
}
