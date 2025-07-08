package com.jotoai.voenix.shop.mugs.controller

import com.jotoai.voenix.shop.mugs.dto.CreateMugCategoryRequest
import com.jotoai.voenix.shop.mugs.dto.MugCategoryDto
import com.jotoai.voenix.shop.mugs.dto.UpdateMugCategoryRequest
import com.jotoai.voenix.shop.mugs.service.MugCategoryService
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
@RequestMapping("/api/mug-categories")
class MugCategoryController(
    private val mugCategoryService: MugCategoryService,
) {
    @GetMapping
    fun getAllCategories(): ResponseEntity<List<MugCategoryDto>> = ResponseEntity.ok(mugCategoryService.getAllCategories())

    @GetMapping("/{id}")
    fun getCategoryById(
        @PathVariable id: Long,
    ): ResponseEntity<MugCategoryDto> = ResponseEntity.ok(mugCategoryService.getCategoryById(id))

    @GetMapping("/search")
    fun searchCategories(
        @RequestParam name: String,
    ): ResponseEntity<List<MugCategoryDto>> = ResponseEntity.ok(mugCategoryService.searchCategoriesByName(name))

    @PostMapping
    fun createCategory(
        @Valid @RequestBody request: CreateMugCategoryRequest,
    ): ResponseEntity<MugCategoryDto> {
        val createdCategory = mugCategoryService.createCategory(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory)
    }

    @PutMapping("/{id}")
    fun updateCategory(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateMugCategoryRequest,
    ): ResponseEntity<MugCategoryDto> = ResponseEntity.ok(mugCategoryService.updateCategory(id, request))

    @DeleteMapping("/{id}")
    fun deleteCategory(
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        mugCategoryService.deleteCategory(id)
        return ResponseEntity.noContent().build()
    }
}
