package com.jotoai.voenix.shop.domain.mugs.service

import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.domain.mugs.dto.CreateMugSubCategoryRequest
import com.jotoai.voenix.shop.domain.mugs.dto.MugSubCategoryDto
import com.jotoai.voenix.shop.domain.mugs.dto.UpdateMugSubCategoryRequest
import com.jotoai.voenix.shop.domain.mugs.entity.MugSubCategory
import com.jotoai.voenix.shop.domain.mugs.repository.MugCategoryRepository
import com.jotoai.voenix.shop.domain.mugs.repository.MugSubCategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MugSubCategoryService(
    private val mugSubCategoryRepository: MugSubCategoryRepository,
    private val mugCategoryRepository: MugCategoryRepository,
) {
    fun getAllSubCategories(): List<MugSubCategoryDto> = mugSubCategoryRepository.findAll().map { it.toDto() }

    fun getSubCategoryById(id: Long): MugSubCategoryDto =
        mugSubCategoryRepository
            .findById(id)
            .map { it.toDto() }
            .orElseThrow { ResourceNotFoundException("MugSubCategory", "id", id) }

    fun getSubCategoriesByCategoryId(categoryId: Long): List<MugSubCategoryDto> =
        mugSubCategoryRepository.findByMugCategoryId(categoryId).map { it.toDto() }

    fun searchSubCategoriesByName(name: String): List<MugSubCategoryDto> =
        mugSubCategoryRepository.findByNameContainingIgnoreCase(name).map { it.toDto() }

    fun searchSubCategoriesByCategoryAndName(
        categoryId: Long,
        name: String,
    ): List<MugSubCategoryDto> =
        mugSubCategoryRepository.findByMugCategoryIdAndNameContainingIgnoreCase(categoryId, name).map { it.toDto() }

    @Transactional
    fun createSubCategory(request: CreateMugSubCategoryRequest): MugSubCategoryDto {
        val mugCategory =
            mugCategoryRepository
                .findById(request.mugCategoryId)
                .orElseThrow { ResourceNotFoundException("MugCategory", "id", request.mugCategoryId) }

        if (mugSubCategoryRepository.existsByMugCategoryIdAndNameIgnoreCase(request.mugCategoryId, request.name)) {
            throw IllegalArgumentException("Subcategory with name '${request.name}' already exists in this category")
        }

        val subCategory =
            MugSubCategory(
                mugCategory = mugCategory,
                name = request.name,
                description = request.description,
            )

        val savedSubCategory = mugSubCategoryRepository.save(subCategory)
        return savedSubCategory.toDto()
    }

    @Transactional
    fun updateSubCategory(
        id: Long,
        request: UpdateMugSubCategoryRequest,
    ): MugSubCategoryDto {
        val subCategory =
            mugSubCategoryRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("MugSubCategory", "id", id) }

        request.mugCategoryId?.let { newCategoryId ->
            val newCategory =
                mugCategoryRepository
                    .findById(newCategoryId)
                    .orElseThrow { ResourceNotFoundException("MugCategory", "id", newCategoryId) }
            subCategory.mugCategory = newCategory
        }

        request.name?.let { newName ->
            val categoryId = subCategory.mugCategory.id!!
            if (newName != subCategory.name &&
                mugSubCategoryRepository.existsByMugCategoryIdAndNameIgnoreCase(categoryId, newName)
            ) {
                throw IllegalArgumentException("Subcategory with name '$newName' already exists in this category")
            }
            subCategory.name = newName
        }

        request.description?.let { subCategory.description = it }

        val updatedSubCategory = mugSubCategoryRepository.save(subCategory)
        return updatedSubCategory.toDto()
    }

    @Transactional
    fun deleteSubCategory(id: Long) {
        if (!mugSubCategoryRepository.existsById(id)) {
            throw ResourceNotFoundException("MugSubCategory", "id", id)
        }
        mugSubCategoryRepository.deleteById(id)
    }
}
