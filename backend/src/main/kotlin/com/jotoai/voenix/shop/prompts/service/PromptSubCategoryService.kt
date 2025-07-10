package com.jotoai.voenix.shop.prompts.service

import com.jotoai.voenix.shop.common.exception.ResourceAlreadyExistsException
import com.jotoai.voenix.shop.common.exception.ResourceNotFoundException
import com.jotoai.voenix.shop.prompts.dto.CreatePromptSubCategoryRequest
import com.jotoai.voenix.shop.prompts.dto.PromptSubCategoryDto
import com.jotoai.voenix.shop.prompts.dto.UpdatePromptSubCategoryRequest
import com.jotoai.voenix.shop.prompts.entity.PromptSubCategory
import com.jotoai.voenix.shop.prompts.repository.PromptCategoryRepository
import com.jotoai.voenix.shop.prompts.repository.PromptRepository
import com.jotoai.voenix.shop.prompts.repository.PromptSubCategoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PromptSubCategoryService(
    private val promptSubCategoryRepository: PromptSubCategoryRepository,
    private val promptCategoryRepository: PromptCategoryRepository,
    private val promptRepository: PromptRepository,
) {
    fun getAllPromptSubCategories(): List<PromptSubCategoryDto> =
        promptSubCategoryRepository.findAll().map { subcategory ->
            PromptSubCategoryDto(
                id = subcategory.id!!,
                promptCategoryId = subcategory.promptCategory.id!!,
                name = subcategory.name,
                description = subcategory.description,
                promptsCount = promptRepository.countBySubcategoryId(subcategory.id),
                createdAt = subcategory.createdAt,
                updatedAt = subcategory.updatedAt,
            )
        }

    fun getPromptSubCategoryById(id: Long): PromptSubCategoryDto =
        promptSubCategoryRepository
            .findById(id)
            .map { subcategory ->
                PromptSubCategoryDto(
                    id = subcategory.id!!,
                    promptCategoryId = subcategory.promptCategory.id!!,
                    name = subcategory.name,
                    description = subcategory.description,
                    promptsCount = promptRepository.countBySubcategoryId(subcategory.id),
                    createdAt = subcategory.createdAt,
                    updatedAt = subcategory.updatedAt,
                )
            }.orElseThrow { ResourceNotFoundException("PromptSubCategory", "id", id) }

    fun getPromptSubCategoriesByCategory(categoryId: Long): List<PromptSubCategoryDto> =
        promptSubCategoryRepository.findByPromptCategoryId(categoryId).map { subcategory ->
            PromptSubCategoryDto(
                id = subcategory.id!!,
                promptCategoryId = subcategory.promptCategory.id!!,
                name = subcategory.name,
                description = subcategory.description,
                promptsCount = promptRepository.countBySubcategoryId(subcategory.id),
                createdAt = subcategory.createdAt,
                updatedAt = subcategory.updatedAt,
            )
        }

    fun searchPromptSubCategoriesByName(name: String): List<PromptSubCategoryDto> =
        promptSubCategoryRepository.findByNameContainingIgnoreCase(name).map { subcategory ->
            PromptSubCategoryDto(
                id = subcategory.id!!,
                promptCategoryId = subcategory.promptCategory.id!!,
                name = subcategory.name,
                description = subcategory.description,
                promptsCount = promptRepository.countBySubcategoryId(subcategory.id),
                createdAt = subcategory.createdAt,
                updatedAt = subcategory.updatedAt,
            )
        }

    @Transactional
    fun createPromptSubCategory(request: CreatePromptSubCategoryRequest): PromptSubCategoryDto {
        val promptCategory =
            promptCategoryRepository
                .findById(request.promptCategoryId)
                .orElseThrow { ResourceNotFoundException("PromptCategory", "id", request.promptCategoryId) }

        if (promptSubCategoryRepository.existsByPromptCategoryIdAndName(request.promptCategoryId, request.name)) {
            throw ResourceAlreadyExistsException("PromptSubCategory", "name", request.name)
        }

        val promptSubCategory =
            PromptSubCategory(
                promptCategory = promptCategory,
                name = request.name,
                description = request.description,
            )

        val savedPromptSubCategory = promptSubCategoryRepository.save(promptSubCategory)
        return PromptSubCategoryDto(
            id = savedPromptSubCategory.id!!,
            promptCategoryId = savedPromptSubCategory.promptCategory.id!!,
            name = savedPromptSubCategory.name,
            description = savedPromptSubCategory.description,
            promptsCount = 0,
            createdAt = savedPromptSubCategory.createdAt,
            updatedAt = savedPromptSubCategory.updatedAt,
        )
    }

    @Transactional
    fun updatePromptSubCategory(
        id: Long,
        request: UpdatePromptSubCategoryRequest,
    ): PromptSubCategoryDto {
        val promptSubCategory =
            promptSubCategoryRepository
                .findById(id)
                .orElseThrow { ResourceNotFoundException("PromptSubCategory", "id", id) }

        request.promptCategoryId?.let { categoryId ->
            val promptCategory =
                promptCategoryRepository
                    .findById(categoryId)
                    .orElseThrow { ResourceNotFoundException("PromptCategory", "id", categoryId) }
            promptSubCategory.promptCategory = promptCategory
        }

        request.name?.let {
            if (promptSubCategoryRepository.existsByPromptCategoryIdAndName(
                    promptSubCategory.promptCategory.id!!,
                    it,
                ) &&
                promptSubCategory.name != it
            ) {
                throw ResourceAlreadyExistsException("PromptSubCategory", "name", it)
            }
            promptSubCategory.name = it
        }
        request.description?.let { promptSubCategory.description = it }

        val updatedPromptSubCategory = promptSubCategoryRepository.save(promptSubCategory)
        return PromptSubCategoryDto(
            id = updatedPromptSubCategory.id!!,
            promptCategoryId = updatedPromptSubCategory.promptCategory.id!!,
            name = updatedPromptSubCategory.name,
            description = updatedPromptSubCategory.description,
            promptsCount = promptRepository.countBySubcategoryId(updatedPromptSubCategory.id),
            createdAt = updatedPromptSubCategory.createdAt,
            updatedAt = updatedPromptSubCategory.updatedAt,
        )
    }

    @Transactional
    fun deletePromptSubCategory(id: Long) {
        if (!promptSubCategoryRepository.existsById(id)) {
            throw ResourceNotFoundException("PromptSubCategory", "id", id)
        }
        promptSubCategoryRepository.deleteById(id)
    }
}
